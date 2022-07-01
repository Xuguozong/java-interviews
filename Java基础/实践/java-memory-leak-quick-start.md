### java-memory-leak quick start
- 内存泄露的危害

  - 服务降级

  - OOM

  - 毫无征兆或奇怪的程序崩溃

  - 耗尽连接资源

- 内存泄露场景及处置办法

  - 大量使用**静态字段**
    	静态字段通常伴随着应用程序的生命周期(**除非它的 ClassLoader 被垃圾回收**了)
    	建议：
    		尽量少用静态变量
    		使用单例时，使用懒加载模式

  - **未关闭的资源**
    	未关闭的资源会消耗内存
    	建议：
    		使用 finally 块关闭打开的资源或使用 try-with-resources 语法

  -  **不好的 equals() 和 hashCode() 的实现**
    	HashSet/HashMap 等 有使用到 equals() 和 hashCode() 的地方

  - **内部类引用了外部对象**

  - finalize() 方法

  - 调用大 String 的 intern() 方法

  - **ThreadLocal**

    - 场景：ThreadPool 中的线程(线程复用不会被回收)创建的 ThreadPool 变量没有手动删除(remove 方法)
    - 使用建议，不需要的时候清理掉 **`remove`** 方法，而**不是 #set(null)**
    - 可以尝试 try...finally 的方式释放

    ```java
    try {
        threadLocal.set(System.nanoTime());
        //... further processing
    }
    finally {
        threadLocal.remove();
    }
    ```

    

- 其他建议

  - 内存监控工具的使用 JMC、MAT、 Java VisualVM 、Jprofiler、 YourKit 、JFR 等等
  - 添加 GC 参数：**`-verbose:gc`**

  ![verbose-garbage-collection](verbose-garbage-collection.jpg)

  - [使用软引用代替直接引用](https://www.baeldung.com/java-soft-references)
  - IDE 代码内存泄露警告
  - [Benchmarking](https://www.baeldung.com/java-microbenchmark-harness)

[参考](https://www.baeldung.com/java-memory-leaks)

