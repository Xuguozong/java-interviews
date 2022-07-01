### 1. Broker 四大组件配置

```java
final BrokerController controller = new BrokerController(
                brokerConfig,
                nettyServerConfig,
                nettyClientConfig,
                messageStoreConfig);
```

- brokerConfig
  - broker 各种配置,包括 mq home 目录，namesev 地址，brokerName,brokerId,各种线程池大小以及队列大小等等
- nettyServerConfig
  - 作为 Netty 服务端的一些相关配置,包括监听地址,boss 线程数,worker线程数,socket 发送及接受缓存区大小等等
- nettyClientConfig
  - 作为 Netty 客户端(服务端为 Namesrv)的一些相关配置
- messageStoreConfig
  - 消息存储相关的配置,包括消息/commitLog 存储目录,Mapped 文件大小,刷盘相关配置等等