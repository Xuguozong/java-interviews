### Spring 方面调整

- 懒加载

  > 配置：**spring.main.lazy-initialization=true**

  - 缺点：
    - 第一次访问会涉及 bean 的初始化导致请求变慢
    - 启动阶段一些可能暴露的问题可能到运行时才能发现，如 ClassNotFoundException

- 去除不必要的自动配置项

  > 配置：**logging.level.org.springframework.boot.autoconfigure=DEBUG**

  - 关注 **CONDITIONS EVALUATION REPORT** 下面打印的自动配置类，去除不需要的

- 适用 Undertow 替代 Tomcat

- java -jar 启动时指定配置文件避免扫描搜索配置文件

  > **java -jar .\target\springStartupApp.jar --spring.config.location=classpath:/application.properties**

- 使用索引加速 

  ```
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context-indexer</artifactId>
      <version>${spring.version}</version>
      <optional>true</optional>
  </dependency>
  ```

- 禁用 jmx

  > **spring.jmx.enabled=false**

  

### JVM 方面

- 去除字节码版本验证

  - **-Xverify**：默认，验证非 boot loader 的字节码
  - **-Xverify:all **: 验证所有
  - **-Xverify:none 或者 -Xnoverify**: 不验证所有

  > **java -jar -noverify .\target\springStartupApp.jar**

- 禁用 C2 编译

  - **-XX:-TieredCompilation**

  > **java -jar -XX:TieredStopAtLevel=1 -noverify .\target\springStartupApp.jar**

### Spring Native



[spring-boot-startup-speed](https://www.baeldung.com/spring-boot-startup-speed)