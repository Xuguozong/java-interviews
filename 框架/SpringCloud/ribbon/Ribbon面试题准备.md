1. spring cloud 里面调用的时候，底层是用什么发送http请求的，是httpclient还是okhttp这些？

2. #### Ribbon的负载均衡策略有哪些？

3. Ribbon是怎么实现灰度发布的？

4. ribbon原理

   > 1. 整合 Eureka 获取服务实例列表
   > 2. 获取所有 RestTemplate 实例，添加 拦截器 来拦截所有 http 请求
   > 3. 通过 IRule 负载均衡算法来选择实际的服务实例地址
   > 4. 发送请求

5. ribbon相关的问题，路由策略，怎么做到下一次选择server的时候我知道上次选择了哪个？