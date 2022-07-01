### 总体目标

> 尽可能在新生代GC，让FGC尽量不要发生，即使发生了，也要尽量降低YGC和FGC的频率

### 1. GC日志监控

- GC日志打印

  ```
  -Xloggc:gc.log --> 1.8
  -Xlog:gc.log   --> 11
  -XX:+PrintGCDetails --> 详细信息
  -XX:+PrintGCTimeStamps  --> 时间信息
  -XX:+UseGCLogFileRotation --> 滚动日志
  -XX:NumberOfGCLogFiles=5  --> 日志文件个数
  ```

- YGC 频率和平均STW时间

- FGC频率和平均STW时间

- 辅助工具

  - [gceasy](https://gceasy.io/)

### 2. 业务的GC运行模型的分析和建立

- 评估单个请求创建多少对象和占用多少内存
- 评估QPS
- 评估单个请求的平均延时
- 评估业务对请求响应延时的敏感程度(后台系统还是C端系统)

### 3. 根据模型调优

- 大内存使用G1
- 新生代内存大小及比例分配是否合理

