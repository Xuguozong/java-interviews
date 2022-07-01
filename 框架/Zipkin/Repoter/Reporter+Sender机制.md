### 主要的类及它们之间的关系

- Reporter 接口
  - report(span) 方法
- AsyncReporter 抽象类
  - BoundAsyncReporter 实现类
    - Sender 属性用于发送编码后的 span 数据
    - ByteBoundedQueue 队列属性用于存放 report 进来的 span
  - Fluhser 后台刷 span 任务类
    - BoundAsyncReporter 属性用于 flush
    - BufferNextMessage 属性用于将一堆 span 归于一个 message
- Sender 抽象类
  - 用于发送编码后的 span 字节数据
- ByteBoundedQueue
  - 用于缓存 report 过来的 span 列表数据
  - 实现类似 ArrayBlockingQueue,底层数组，使用不公平重入锁和信号量来实现队列的阻塞特性，使用读写指针来定位读写位置
- BufferNextMessage
  - 将 ByteBoundedQueue 中的 span 列表数据搞过来组成一个 message

### 线程模型

- Reporter 的 flush 线程

  - 后台循环的单线程，循环间隔时间是自定义的 timeoutNanos
  - 到时就调用 sender 去发送缓存中的 spans 数据

- OkHttpSender 的 dispatchExecutor 线程池

  - 构造函数：

    ```java
    // bound the executor so that we get consistent performance
    ThreadPoolExecutor dispatchExecutor =
          new ThreadPoolExecutor(0, maxRequests, 60, TimeUnit.SECONDS,
            // Using a synchronous queue means messages will send immediately until we hit max
            // in-flight requests. Once max requests are hit, send will block the caller, which is
            // the AsyncReporter flush thread. This is ok, as the AsyncReporter has a buffer of
            // unsent spans for this purpose.
            new SynchronousQueue<>(),
            OkHttpSenderThreadFactory.INSTANCE);
    ```

  - 要点：

    - 通过 **SynchronousQueue** 约束线程池来得到一致的性能表现
    - 在达到设定的最大请求数(默认64)之前，来一个发送请求就马上发送了，达到阈值后，就会阻塞调用线程，也就是 AsyncReporter flush thread，是没有什么关系的，因为 AsyncReporter 有一个未发送的 spans buffer

### 主要数据结构

### 批量发送以及压缩算法的实现

### 最坏情况下内存占用的估算

### JUC 同步工具和锁的应用

### 本质解决的是什么问题