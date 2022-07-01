### 1. 负载均衡器 ILoadBalancer

```java
ILoadBalancer balancer = new BaseLoadBalancer();

List<Server> servers = new ArrayList<Server>();
servers.add(new Server(“localhost”, 8080));
servers.add(new Server(“localhost”, 8088));
balancer.addServers(servers);

for(int i = 0; i < 10; i++) {
    Server server = balancer.chooseServer(null);
    System.out.println(server);
}
```



### 2. 负载均衡规则 IRule

> 从服务器列表中选取一个出来

**内置负载均衡规则：**

- **RoundRobinRule:**  轮询规则
- **AvailabilityFilteringRule: ** 服务可用性规则
  - 如果3次连接失败，就会等待30秒后再次访问；如果不断失败，那么等待时间会不断变长，如果某个服务器的并发请求太高了，那么会绕过去，不再访问
- **WeightedResponseTimeRule: **带权重规则
- **ZoneAvoidanceRule:** 机房物理位置规则
- **BestAvailableRule:** 忽略失败的服务器，找并发低的
- **RandomRule：** 随机规则
- **RetryRule：** 可以重试的



### 3.  服务器检测 IPing

```java
balancer.setPing(new PingUrl());
// 这里就会每隔1秒去请求那两个地址
balancer.setPingInterval(1);
```

