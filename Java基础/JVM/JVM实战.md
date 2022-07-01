### 1. spring-boot-devtools 造成的本地开发环境 Eden 区频繁回收

> 背景：本地开发环境 + VisualGC 插件观察

> 现象：Eden 区内存快速稳步增长，定时 GC

> 查看：jmap -histo [pid] 查看类加载的信息，对比不同时段类增长的情况

```
发现 org.springframework.boot.devtools.filewatch.FileSnapshot 及其他的一些基础 Java 类在稳步增长，推测是 devtools 热部署在实时扫描源码文件变化而新建的对象造成
```

> 验证

```
# 1. 去除 spring-boot-devtools 依赖，Eden 区在没有请求的情况下不变
# 2. java -jar 的方式启动应用， Eden 也没有增长
# 3. <optional>true</optional> 似乎对 jar 方式启动的应用无影响(devtools未生效)
```

