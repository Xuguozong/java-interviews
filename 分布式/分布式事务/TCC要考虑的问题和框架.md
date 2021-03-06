### 要考虑的问题

- 接口拆分问题
  - try 接口
    - 锁定资源，不需要的话就置空
  - confirm 接口
    - 原来的业务方法
  - cancel 接口
    - 提供回滚的方法
- 接口的几种特殊情况
  - 空回滚
    - try 阶段网络故障，就不能直接调用 cancel 接口，而是啥都不干
  - try 回滚以及 confirm 回滚
    - 任意阶段失败都需要回滚
  - 倒置请求
    - 三个阶段间的网络调用超时问题
- 接口的幂等性保证
  - 分布式接口幂等性问题必须依赖第三方中间件来实现
  - 可以考虑使用经典的 zk