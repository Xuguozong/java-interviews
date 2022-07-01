### 计算Java对象大小的几种方法

1. Instrumentation#getObjectSize

   ```
   # 声明 premain 方法
   public class SizeTool {
       private static Instrumentation instrumentation;
   
       public static void premain(String args, Instrumentation inst) {
           instrumentation = inst;
       }
   
       public static long getObjectSize(Object o) {
           return instrumentation.getObjectSize(o);
       }
   }
   # 打jar包 -> b.jar
   在 MANIFEST.MF 文件中声明：
   Manifest-Version: 1.0
   Main-Class: yp.tools.Main
   Premain-Class: yp.tools.SizeTool
   
   # 以 javaagent 的方式执行
   java -javaagent:b.jar -jar a.jar
   ```

2. Unsafe#objectFieldOffset(Field field)

   ```
   可以得到示例属性数据的最大偏移量，再加上属性类型值 + padding 填充大致计算出占用内存大小
   ```

3. lucene-core 的 RamUsageEstimator

   ```
   通过计算 Java 对象头、实例数据、引用等计算占用大小，缺点：基于JVM声明规范而不是运行时内存地址计算，可能不是很准
   //计算指定对象及其引用树上的所有对象的综合大小，单位字节
   long RamUsageEstimator.sizeOf(Object obj)
   
   //计算指定对象本身在堆空间的大小，单位字节
   long RamUsageEstimator.shallowSizeOf(Object obj)
   
   //计算指定对象及其引用树上的所有对象的综合大小，返回可读的结果，如：2KB
   String RamUsageEstimator.humanSizeOf(Object obj)
   
   注意：以上方法基于 lucene-core 4.0.0 版本
   ```

4. [JOL](https://openjdk.java.net/projects/code-tools/jol/)    [github地址](https://github.com/openjdk/jol)      [IDEA插件](https://github.com/stokito/IdeaJol)    [参考](https://www.baeldung.com/java-memory-layout)

   ```
   <dependency>
       <groupId>org.openjdk.jol</groupId>
       <artifactId>jol-core</artifactId>
       <version>0.16</version>
   </dependency>
   ```

   

### Java 对象格式

1. 总体

2. 对象头

   2.1 `MarkWord`

   ```
   // The markOop describes the header of an object.
   //
   // Note that the mark is not a real oop but just a word.
   // It is placed in the oop hierarchy for historical reasons.
   //
   // Bit-format of an object header (most significant first, big endian layout below):
   // 以下是大端序排序的对象头二进制格式
   
   //  32 bits:
   //  --------
   //             hash:25 ------------>| age:4    biased_lock:1 lock:2 (normal object)
   //             JavaThread*:23 epoch:2 age:4    biased_lock:1 lock:2 (biased object)
   //             size:32 ------------------------------------------>| (CMS free block)
   //             PromotedObject*:29 ---------->| promo_bits:3 ----->| (CMS promoted object)
   
   //
   //  64 bits:
   //  --------
   //  unused:25 hash:31 -->| unused:1   age:4    biased_lock:1 lock:2 (normal object)
   //  JavaThread*:54 epoch:2 unused:1   age:4    biased_lock:1 lock:2 (biased object)
   //  PromotedObject*:61 --------------------->| promo_bits:3 ----->| (CMS promoted object)
   //  size:64 ----------------------------------------------------->| (CMS free block)
   //
   //  unused:25 hash:31 -->| cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && normal object)
   //  JavaThread*:54 epoch:2 cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && biased object)
   //  narrowOop:32 unused:24 cms_free:1 unused:4 promo_bits:3 ----->| (COOPs && CMS promoted object)
   //  unused:21 size:35 -->| cms_free:1 unused:7 ------------------>| (COOPs && CMS free block)
   //
   //  - hash contains the identity hash value: largest value is
   //    31 bits, see os::random().  Also, 64-bit vm's require
   //    a hash value no bigger than 32 bits because they will not
   //    properly generate a mask larger than that: see library_call.cpp
   //    and c1_CodePatterns_sparc.cpp.
   ##	最大 31 位，由 os::random() 方法生成，不管是不是 final 修饰，都是 lazy initialization 的
   //
   //  - the biased lock pattern is used to bias a lock toward a given
   //    thread. When this pattern is set in the low three bits, the lock
   //    is either biased toward a given thread or "anonymously" biased,
   //    indicating that it is possible for it to be biased. When the
   //    lock is biased toward a given thread, locking and unlocking can
   //    be performed by that thread without using atomic operations.
   //    When a lock's bias is revoked, it reverts back to the normal
   //    locking scheme described below.
   ##  偏向锁标志位设置在低 3 位
   //
   //    Note that we are overloading the meaning of the "unlocked" state
   //    of the header. Because we steal a bit from the age we can
   //    guarantee that the bias pattern will never be seen for a truly
   //    unlocked object.
   //
   //    Note also that the biased state contains the age bits normally
   //    contained in the object header. Large increases in scavenge
   //    times were seen when these bits were absent and an arbitrary age
   //    assigned to all biased objects, because they tended to consume a
   //    significant fraction of the eden semispaces and were not
   //    promoted promptly, causing an increase in the amount of copying
   //    performed. The runtime system aligns all JavaThread* pointers to
   //    a very large value (currently 128 bytes (32bVM) or 256 bytes (64bVM))
   //    to make room for the age bits & the epoch bits (used in support of
   //    biased locking), and for the CMS "freeness" bit in the 64bVM (+COOPs).
   //
   //    [JavaThread* | epoch | age | 1 | 01]       lock is biased toward given thread
   //    [0           | epoch | age | 1 | 01]       lock is anonymously biased
   //
   //  - the two lock bits are used to describe three states: locked/unlocked and monitor.
   //
   //    [ptr             | 00]  locked             ptr points to real header on stack
   //    [header      | 0 | 01]  unlocked           regular object header
   //    [ptr             | 10]  monitor            inflated lock (header is wapped out)
   //    [ptr             | 11]  marked             used by markSweep to mark an object
   //                                               not valid at any other time
   //
   //    We assume that stack/thread pointers have the lowest two bits cleared.
   ```

3. 实例数据

4. 对齐填充

   - 保证是 8 bytes 的倍数，这样可以通过 << 3 的操作节省 3 bit 空间来存储长度信息



[参考](https://www.cnblogs.com/E-star/p/10222250.html)

[markOop.hpp](http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/87ee5ee27509/src/share/vm/oops/markOop.hpp)