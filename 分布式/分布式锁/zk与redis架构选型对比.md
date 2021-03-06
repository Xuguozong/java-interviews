- Redis

  > 你对redis分布式锁的使用，用是可以用的，但是你要知道他的优势和劣势，如果你可以容忍一些劣势，以及做一些对应的措施和预案，后台可以容忍做补数据的操作，而且你对分布式锁的需求很旺盛，需要人家各种高级分布式锁的一些支持

  - 单实例方案
    - 做不到高可用，除非你是那种不太核心的小系统，随便用一下分布式锁，你可以这么弄，应该还好，毕竟对高可用要求没那么高
  - redis 主从 + 哨兵
    - 保证高可用，master宕机，slave接替，但是有隐患，master宕机的一瞬间，还没异步复制锁到slave，导致重复加锁的问题，高可用是高可用了，但是锁的实现有漏洞，可能导致系统异常
  - Redlock
    - redlock算法，个人不推荐，实现过程太复杂繁琐，很脆弱，多节点同时设置分布式锁，但是失效时间都不一样，随着不同的linux机器的时间不同步，以及各种你无法考虑到的问题，很可能出现重复加锁
    - 有两个问题，第一是实现过程和步骤太复杂，上锁的过程和机制很重，很复杂，导致很脆弱，各种意想不到的情况都可能发生；第二是并不健壮，不一定能完全实现健壮的分布式锁的语义
  - 优点：
    - redisson：redis分布式锁的支持，非常不错，可重入锁、读写锁、公平锁、信号量、CountDownLatch，很多种复杂的锁的语义，可以支持我们将分布式锁玩儿的非常的好

- ZK

  > 如果有zookeeper的环境，而且你的分布式锁的需求很简单，就是普通的悲观锁模型就可以了，不涉及到什么公平锁、读写锁之类的东西，那么可以考虑基于zk的分布式锁模型，健壮，稳定，zk的临时顺序节点实现的分布式锁，其实那套模型挺健壮的，zk本身就是集群多节点分布式高可用，分布式系统协调、处理的，支持分布式锁的时候，基于zk的一些语义，监听节点的变化

  - 优点是锁模型健壮、稳定、可用性高
  - 缺点是目前没有太好的开源的zk分布式锁的类库，封装多种不同的锁类型，因为有可重入锁、读写锁、公平锁等多种锁类型，但是zk的话，目前常见的方案，就是自己动手封装一个基于顺序节点的普通的悲观锁，没有复杂的锁机制的支持