### 1. DiscoveryClient(服务发现客户端)

> @EnableDiscoveryClient 
>
> This looks for implementations of the `DiscoveryClient` and `ReactiveDiscoveryClient` interfaces with `META-INF/spring.factories`. Implementations of the discovery client add a configuration class to `spring.factories` under the `org.springframework.cloud.client.discovery.EnableDiscoveryClient` key

-  [Spring Cloud Netflix Eureka](https://cloud.spring.io/spring-cloud-netflix/)

- [Spring Cloud Consul Discovery](https://cloud.spring.io/spring-cloud-consul/)

-  [Spring Cloud Zookeeper Discovery](https://cloud.spring.io/spring-cloud-zookeeper/)

- spring cloud alibaba nacos

- SimpleDiscoveryClient

  > If there is no Service-Registry-backed `DiscoveryClient` in the classpath, `SimpleDiscoveryClient` instance, that uses properties to get information on service and instances, will be used.
  >
  > The information about the available instances should be passed to via properties in the following format: `spring.cloud.discovery.client.simple.instances.service1[0].uri=http://s11:8080`, where `spring.cloud.discovery.client.simple.instances` is the common prefix, then `service1` stands for the ID of the service in question, while `[0]` indicates the index number of the instance (as visible in the example, indexes start with `0`), and then the value of `uri` is the actual URI under which the instance is available.



### 2. ServiceRegistry(服务注册中心)

> Commons now provides a `ServiceRegistry` interface that provides methods such as `register(Registration)` and `deregister(Registration)`, which let you provide custom registered services. `Registration` is a marker interface

- `ZookeeperRegistration` used with `ZookeeperServiceRegistry`
- `EurekaRegistration` used with `EurekaServiceRegistry`
- `ConsulRegistration` used with `ConsulServiceRegistry`



### 3. LoadBalancer(负载均衡器)

> [Spring Cloud LoadBalancer starter](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#spring-cloud-loadbalancer-starter)

> Spring Cloud provides its own client-side load-balancer abstraction and implementation. For the load-balancing mechanism, `ReactiveLoadBalancer` interface has been added and a **Round-Robin-based** and **Random** implementations have been provided for it. In order to get instances to select from reactive `ServiceInstanceListSupplier` is used. Currently we support a service-discovery-based implementation of `ServiceInstanceListSupplier` that retrieves available instances from Service Discovery using a [Discovery Client](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#discovery-client) available in the classpath.

- RestTemplate
- WebClient
- Netflix Ribbon



### 4. CircuitBreaker(断路器)

- Netflix Hystrix
- [Resilience4J](https://github.com/resilience4j/resilience4j)
- [Sentinel](https://github.com/alibaba/Sentinel)
- [Spring Retry](https://github.com/spring-projects/spring-retry)

