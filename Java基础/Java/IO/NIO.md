## 核心组件

### 1. Channel

*代表一个到实体(如硬件设备、文件、socket或一个能够执行一个或多个不同I/O操作的程序组件)的开放连接，可以被打开、关闭、读写操作，是该实体中数据流的载体，数据可以从 Channel 读到 Buffer，也可以将 Buffer 写到 Channel*

![Channel与Buffer关系](imgs/overview-channels-buffers1.png)

**Channel 的主要实现**

- FileChannel

  - 两个 Channel 中只要有一个是 FileChannel，就可以相互传输数据
  - FileChannel 无法设置为非阻塞模式，它总是运行在阻塞模式下。它无法直接打开，需要通过InputStream,OutputStream 或 RandomAcsessFile 来获取一个 FileChannel 实例。
  - force(true) 方法同时将 channel 的文件数据和元数据强制写到磁盘上。

- DatagramChannel

  - 因为 UDP 是无连接的网络协议，所以不能像其它通道那样读取和写入。它发送和接收的是数据包。

- SocketChannel

  - 打开一个 SocketChannel

  ```java
  SocketChannel socketChannel = SocketChannel.open();
  ```

  - 一个新连接到达 ServerSocketChannel 时会创建一个 SocketChannel

  ```java
  socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
  ```

- ServerSocketChannel

  - ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。

  ```java
  ServerSocketChannel.open();
  serverSocketChannel.socket().bind(new InetSocketAddress(9999));
  serverSocketChannel.configureBlocking(false);
  while(true){
      SocketChannel socketChannel =
              serverSocketChannel.accept();
      if(socketChannel != null){
          //do something with socketChannel...
      }
  }
  ```

  

### 2. Buffer

```
ByteBuffer
CharBuffer
DoubleBuffer
FloatBuffer
IntBuffer
LongBuffer
ShortBuffer
MappedByteBuffer --> 内存映射
```

**用法及关键方法**

```java
## 使用 Buffer 读写数据的一般步骤
1. 写入数据到 Buffer
2. 调用 flip() 方法
3. 从 Buffer 读取数据
4. 调用 clear() 或 compact()方法

flip()     写模式切换到读模式，position 设置为0，limit 设置为之前 position 的值
clear()    清空整个缓冲区
compact()  清除读过的数据，剩下数据移到缓冲区起始处
例：
andomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();
//create buffer with capacity of 48 bytes
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = inChannel.read(buf); //read into buffer.
while (bytesRead != -1) {
  buf.flip();  //make buffer ready for read
  while(buf.hasRemaining()){
      System.out.print((char) buf.get()); // read 1 byte at a time
  }
  buf.clear(); //make buffer ready for writing
  bytesRead = inChannel.read(buf);
}
aFile.close();
```

**Buffer 的数据结构**

*本质是 NIO 对一块内存的抽象封装*

![Buffer的结构](imgs/buffers-modes.png)

```
# capacity 容量
# position 当前数据位置
# limit 写模式下：=capacity，表示可写的空间大小；读模式：=position
```



### 3. Selector

*允许单线程处理多个**低流量Channel**(如聊天服务)*

![overview-selectors](imgs/overview-selectors.png)

> 要使用Selector，得向Selector注册Channel，然后调用它的select()方法。这个方法会一直阻塞到某个注册的通道有事件就绪。一旦这个方法返回，线程就可以处理这些事件，事件的例子有如新连接进来，数据接收等。





### 参考

[并发编程网-Java NIO 系列教程](http://ifeve.com/java-nio-all/)