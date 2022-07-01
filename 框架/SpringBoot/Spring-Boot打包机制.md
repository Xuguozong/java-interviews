### 1. java -jar xxx.jar 是如何执行的

>  打包 jar 的时候会自动生成 **`META-INF/MANIFEST.MF`** 文件, **`MANIFEST.MF`**指定了 manifest 版本、java 版本、已经应用入口类 **`Main-Class`**



### 2. Spring Boot 如何打包为可执行 jar

- **spring-boot-loader** 模块实现了 sb 打包为可执行 jar 或 war 文件

- **Nested jar ** (jar 包内包含 jar 包)的问题

  - java 本身没有提供解决方案

  - spring boot 的解决方案：

    - 特定结构：

    ```
    example.jar
     |
     +-META-INF
     |  +-MANIFEST.MF
     +-org
     |  +-springframework
     |     +-boot
     |        +-loader
     |           +-<spring boot loader classes>
     +-BOOT-INF
        +-classes
        |  +-mycompany
        |     +-project
        |        +-YourClasses.class
        +-lib
           +-dependency1.jar
           +-dependency2.jar
    ```

    - 应用自身 class 在 **`BOOT-INF/classes`** 目录下
    - 依赖 jar 包放在 **`BOOT-INF/lib`** 目录下
    - **`Index Files`**
      - **BOOT-INF/classpath.idx** 记录了 jar 包被添加到 classpath 下的索引
      - **layers.idx** 允许 jar 进行镜像分层

- **`org.springframework.boot.loader.jar.JarFile`**

  - nested jar 的核心抽象类
  - 基于偏移量定位 jar 包内的 class 文件

- **`org.springframework.boot.loader.JarLauncher`**

  - MANIFEST.MF

  ```
  Manifest-Version: 1.0
  Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
  Implementation-Title: afs-api
  Implementation-Version: 1.0.0-RELEASE
  Start-Class: com.afs.app.Application
  Spring-Boot-Classes: BOOT-INF/classes/
  Spring-Boot-Lib: BOOT-INF/lib/
  Build-Jdk-Spec: 1.8
  Spring-Boot-Version: 2.3.1.RELEASE
  Created-By: Maven Jar Plugin 3.2.0
  Implementation-Vendor: Pivotal Software, Inc.
  ## JarLauncher it is used to setup an appropriate URLClassLoader and ultimately call your main() method
  ## purpose is to load resources (.class files and so on) from nested jar files
  Main-Class: org.springframework.boot.loader.JarLauncher
  ```

  

> 参考：

1. [spring-boot-tools](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-tools)
2. [Main-Class vs Start-Class](https://stackoverflow.com/questions/34593127/manifest-mf-difference-between-main-class-and-start-class)
3. [executable-jar](https://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html)
4. [Setting An Application's Entry Point](https://docs.oracle.com/javase/tutorial/deployment/jar/appman.html)