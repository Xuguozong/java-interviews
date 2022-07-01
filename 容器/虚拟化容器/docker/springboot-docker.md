### OCI 容器(Spring Boot 2.3后支持)

1. maven 插件支持

   ```xml
   <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
       <configuration>
           <image>
               <name>afs.com/${project.artifactId}:v1</name>
           </image>
       </configuration>
   </plugin>
   ```

2. 运行命令

   ```shell
   mvn clean -Dmaven.test.skip spring-boot:build-image
   ```

3. 问题

   ```
   1. 网络问题 --> 升级 Spring Boot 到 2.4.1
   ```



### dive 工具查看镜像内分层

1. 安装

   ```
   # RHEL/CentOS
   wget https://github.com/wagoodman/dive/releases/download/v0.9.2/dive_0.9.2_linux_amd64.deb
   sudo apt install ./dive_0.9.2_linux_amd64.deb
   # Ubantu/Debian
   wget https://github.com/wagoodman/dive/releases/download/v0.9.2/dive_0.9.2_linux_amd64.deb
   sudo apt install ./dive_0.9.2_linux_amd64.deb
   ```

2. 使用

   ```
   dive [image-tag]
   ```

3. [github 地址](https://github.com/wagoodman/dive)

### 分阶层构建和 Dockerfile 的改进

1. maven 插件添加分层属性

   ```xml
   <plugin>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-maven-plugin</artifactId>
       <configuration>
           <image>
               <name>afs.com/${project.artifactId}:v1</name>
           </image>
           <layers>
               <enabled>true</enabled>
           </layers>
       </configuration>
   </plugin>
   ```

2.  查看 jar 包的内部分层信息

   ```shell
   [root@host]# java -Djarmode=layertools -jar [target-jar] list
   dependencies
   spring-boot-loader
   snapshot-dependencies
   application
   ```

3.  编写分层的 Dockerfile

   ```dockerfile
   # the first stage of our build will extract the layers
   FROM openjdk:8-jdk-alpine as builder
   WORKDIR application
   ARG JAR_FILE=target/*.jar
   COPY ${JAR_FILE} app.jar
   RUN java -Djarmode=layertools -jar app.jar extract
   
   # the second stage of our build will copy the extracted layers
   FROM openjdk:8-jdk-alpine
   WORKDIR application
   COPY --from=builder application/dependencies ./
   COPY --from=builder application/spring-boot-loader ./
   COPY --from=builder application/snapshot-dependencies ./
   COPY --from=builder application/application/ ./
   ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
   ```

4.  dive 查看镜像层级信息

   ![spring-boot镜像多层分级信息](/imgs/dive-into-multi-stage-spring-boot-images.png)

   



[**Creating Optimized Docker Images for a Spring Boot Application**](https://reflectoring.io/spring-boot-docker/)