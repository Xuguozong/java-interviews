### 1. 前端工程容器化

```
1. npm run build
2. copy dist to nginx
3. copy nginx.conf to nginx container
4. nginx proxy_pass to other backend container
5. base image -- nginx alpine
```

### 2. Spring Boot 工程容器化

- Spring Boot 版本 >= 2.3.0.RELEASE

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>2.3.1.RELEASE</version>
    <configuration>
        <image>
            <name>afs.com/${project.artifactId}:${project.version}</name>
        </image>
        <layers>
            <enabled>true</enabled>
        </layers>
    </configuration>
</plugin>
```

- 简单 Dockerfile

```dockerfile
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
EXPOSE 8080
ENV JAVA_OPTS="\
-server \
-Xmx256m \
-Xms256m \
-Xmn128m 
ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar application.jar
# --spring.datasource.url=${SPRING_DATASOURCE_URL}
```

- 分层 Dockerfile

```dockerfile
# the first stage of our build will extract the layers
FROM openjdk:8-jdk-alpine as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
# RUN java -Djarmode=layertools -jar app.jar extract

# the second stage of our build will copy the extracted layers
FROM openjdk:8-jdk-alpine
WORKDIR application
COPY --from=builder application/dependencies ./
COPY --from=builder application/spring-boot-loader ./
COPY --from=builder application/snapshot-dependencies ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

[Spring Boot2.3 docker](https://blog.csdn.net/boling_cavalry/article/details/106598189)

### 3. 集成 MySQL、Nginx、Spring Boot 的 docker-compose Yaml

```dockerfile
version: '2'
services:
  afs-mysql:
    image: mysql:8.0.22
    ports:
      - 3306:3306
    mem_limit: 2g
    cpus: 8
    environment:
      MYSQL_ROOT_PASSWORD: 123456
    volumes:
      # 指定初始化 sql 文件地址
      - /opt/mysql/init-sql:/docker-entrypoint-initdb.d
      # 指定 MySQL 数据目录
      - /opt/mysql/data:/var/lib/mysql

  afs:
    image: afs:v1
    ports:
      - 8080:8080
    depends_on:
      - afs-mysql
    links:
      - afs-mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://afs-mysql:3306/aifuxiaosheng?autoReconnect=true&useSSL=false&allowMultiQueries=true&useTimezone=true&s&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
  
  afs-web:
    image: afs-web:v1
    depends_on:
      - afs
    links:
      - afs
    ports:
      - 80:80
```

