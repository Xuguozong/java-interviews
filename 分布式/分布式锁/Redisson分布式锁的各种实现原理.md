### 关键实现机制

#### 1. WatchDog 看门狗自动续期机制

> 如果负责储存这个分布式锁的Redisson节点宕机以后，而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。
>
> 为了避免这种情况的发生，Redisson内部提供了一个监控锁的看门狗，它的作用是在Redisson实例被关闭前，不断的延长锁的有效期(默认 10S 执行一次)。默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改[Config.lockWatchdogTimeout](https://github.com/redisson/redisson/wiki/2.-配置方法#lockwatchdogtimeout监控锁的看门狗超时单位毫秒)来另行指定。



#### 2. 通过 leaseTime 参数来指定加锁的时间

> 超过这个时间后锁便自动解开了

```java
// 加锁以后10秒钟自动解锁
// 无需调用unlock方法手动解锁
lock.lock(10, TimeUnit.SECONDS);

// 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
if (res) {
   try {
     ...
   } finally {
       lock.unlock();
   }
}
```



#### 3. 只有拥有锁的进程才能解锁

> `RLock`对象完全符合Java的Lock规范。也就是说**只有拥有锁的进程才能解锁，其他进程解锁则会抛出`IllegalMonitorStateException`错误**。但是如果遇到需要其他进程也能解锁的情况，请使用[分布式信号量`Semaphore`](https://github.com/redisson/redisson/wiki/8.-分布式锁和同步器#86-信号量semaphore) 对象



#### 4. NodeSource source = getNodeSource(key);

> 根据 key 来 hash 到具体的 redis server 的 slot 上来完成加锁的操作



### 1. 可重入锁 - ReentrantLock -- RedissonLock

- **加锁**：在 redis 里设置 **hash 数据结构，生存周期是30000毫秒**

- **维持加锁**：代码里一直加锁，redis 里的 key会一直保持存活，**后台每隔10秒的定时任务（watchdog）不断的检查，只要客户端还在加锁，就刷新key的生存周期为30000毫秒**

- **可重入锁**：同一个线程可以多次加锁，就是**在hash数据结构中将加锁次数累加1**

- **锁互斥**：不同客户端，或者**不同线程，尝试加锁陷入死循环等待**

- **手动释放锁**：可重入锁**自动递减加锁次数**，全部释放锁之后删除锁key

- **宕机自动释放锁**：如果持有锁的**客户端宕机**了，那么此时**后台的watchdog定时调度任务也没了，不会刷新锁key的生存周期，此时 redis 里的锁key会自动释放**

- **尝试加锁超时**：在指定时间内没有成功加锁就**自动退出死循环**，标识本次尝试加锁失败

- **超时锁自动释放**：获取锁之后，在一定时间内没有手动释放锁，则 redis 里的 key 自动过期，自动释放锁

- **锁的存储结构**

  ```
  {
  	“Thread.getId()": 1 // map,key 是加锁线程，value 是加锁次数
  }
  ```

- **其他线程判断自身是否加锁成功**
  - 执行尝试加锁逻辑，返回 long **ttlRemainning** --> 所得剩余存活时间
  - 返回 nil，说明加锁成功
  - 大于 0 说明锁还在，需要循环等待获取

![11_01_redisson-ReentrantLock的原理(4)](imgs/11_01_redisson-ReentrantLock的原理(4).png)



### 2. 公平锁 - Fair Lock -- RedissonFairLock

> 基于Redis的Redisson分布式可重入公平锁也是实现了`java.util.concurrent.locks.Lock`接口的一种`RLock`对象。同时还提供了[异步（Async）](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RLockAsync.html)、[反射式（Reactive）](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RLockReactive.html)和[RxJava2标准](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RLockRx.html)的接口。它保证了当多个Redisson客户端线程同时请求加锁时，优先分配给先发出请求的线程。所有请求线程会在一个队列中排队，当某个线程出现宕机时，Redisson会等待5秒后继续下一个线程，也就是说如果前面有5个线程都处于等待状态，那么后面的线程会等待至少25秒。

```java
RLock fairLock = redisson.getFairLock("anyLock");
// 最常见的使用方法
fairLock.lock();
```

#### 实现原理 RedissonFairLock

- 加锁

> // 存储排队的线程 ID 的列表
> private final String threadsQueueName;
> // 线程id 的 zset 有序集合，**score 是线程至少需要等待的时间**
> private final String timeoutSetName;
>
> 每次其他客户端尝试加锁都会刷新 timeout 值

在一个客户端刚刚加锁之后，其他的客户端来争抢这把锁，刚开始在一定时间范围之内，时间不要过长，各个客户端是可以按照公平的节奏，在队列和有序集合里面进行排序

在一定时间范围内，时间不要过长，其实队列里的元素顺序是不会改变的，各个客户端重新尝试加锁，只不过是刷新有序集合中的分数（timeout），各个客户端的timeout不断加长，但是整体顺序大致还是保持一致的

但是如果客户端A持有的锁的时间过长，timeout，这个所谓的排队是有timeout，可能会在while true死循环中将一些等待时间过长的客户端从队列和有序集合中删除，一旦删除过后，就会发生各个客户端随着自己重新尝试加锁的时间次序，重新进行一个队列中的重排，也就是排队的顺序可能会发生变化

- 释放锁
  - 释放锁的时候，也会走while true的脚本逻辑，看一下有序集合中的元素的timeout时间如果小于了当前时间，就认为他的那个排队就过期了，就删除他，让他后面重新尝试获取锁的时候重排序
  - 如果客户端宕机了，也会导致他就不会重新尝试来获取锁，也就不会刷新有序集合中的timeout分数，不会延长timeout分数，while true的逻辑也可以剔除掉这种宕机的客户端在队列里的占用

![18_02_公平锁原理(2)](imgs/18_02_公平锁原理(2).png)