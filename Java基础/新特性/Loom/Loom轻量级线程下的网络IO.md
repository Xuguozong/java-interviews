### 虚拟线程(Virtual Thread)下网络IO的实现(Loom)
1. 虚拟线程
	
	```
	1）由 JVM 调度的用户线程而不是目前的由 OS 调度的内核线程，JVM 可以支持百万级别的虚拟线程
	2）Platform Thread 1：1 映射内核线程，由 OS 调度，需要更大的栈空间和其他一些需要 OS 维护的资源
	3）一小批 Platform Thread 作为虚拟线程的(无感知的)载体线程(carrier threads).
		锁(Locking)和IO操作作为虚拟线程间的调度点(scheduling points).如一个虚拟线程被 parked 了，就从调度系统中移除了.
	```
	
2. 网络 API 分类
	
	```
	1）异步 - AsynchronousServerSocketChannel, AsynchronousSocketChannel
	2）同步- java.net Socket/ServerSocket/DatagramSocket, 
			java.nio.channels SocketChannel/ServerSocketChannel/DatagramSocketChannel
	3）异步网络 API 和 nio 中设置了 non-blocking 属性的，网络 API 可以直接在虚拟线程中使用，不需要特别的对待
	```
	
3. 同步阻塞网络 API 在虚拟线程下的机制
	
	```
	1）运行在虚拟线程中的同步阻塞网络 API 会自动转换为非阻塞的模式
	2）当网络 IO 阻塞的时候，底层的 native socket 会在 JVM 范围内的事件通知机制(Poller)中注册，然后该虚拟线程会被调度暂停(parked)
	3）当网络 IO 准备就绪的时候(event 到达 Poller),该虚拟线程被重新调度，底层 socket 重试
	4）Poller - event loop 机制
		MacOs:   kqueue
		Linux:   epoll
		Windows: wepoll
		Poller 维护着一个文件描述符(file descriptor)到虚拟线程(virtual thread)的映射.
	```
	
4. Scaling
	
	```
	1）不需要用户自己去实现 event loop 模型并且维护应用跨越 IO 边界的逻辑，全由 JVM 做了(任务调度 + 维护 IO 边界)
	2）虚拟线程默认的调度器是 fork-join work-stealing 调度器，事件通知机制用的 OS 的
	```
	
5. 总结
	1）为了 Loom 项目已经重新实现了 Java 同步网络IO的相关API，[JEP353](https://openjdk.java.net/jeps/353),[JEP373](https://openjdk.java.net/jeps/373)
	2）因为网络 IO 阻塞的虚拟线程会被调度器 parked，IO ready 的时候再 unparked
	
6. 思考

  - 目前的阻塞 IO 是怎么做的？NIO 的非阻塞是怎么做的？

> 翻译总结自:[Networking I/O with Virtual Threads - Under the hood](https://inside.java/2021/05/10/networking-io-with-virtual-threads/)