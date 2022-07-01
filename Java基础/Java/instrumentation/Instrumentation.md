# Instrumentation

### 能够做什么
> 开发者使用instrumentation可以构建一个独立于应用程序的代理程序（agent），用来检测和协助运行在JVM上的程序，甚至能够`替换`和`修改`某些`类的定义`，这样的特性实际上提供了一种**虚拟机级别支持的AOP实现方式**。在java SE 6中更强大：启动后的instrument、本地代码（native code）instrument以及动态改变classpath等。
>
> - APM 产品：pinpoint、skywalking、newrelic
> - 热部署工具：Intellij idea 的 HotSwap、Jrebel
> - Java 诊断工具：Arthas、Btrace、jvm-profiler、easeagent 等

### 实现原理
> 依赖于 **JVMTI （Java Virtual Machine Tool Interface）**本地编程接口集合。

 [JVMTI官方文档](https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html)

### 基本功能和用法
> 功能：通过代理，在main函数运行前后动态地改变类的定义和其他处理操作
> 具体包括：premain, agentmain,动态改变classpath,本地方法prefix等

**用法**： 参考 [Instrumentation 的基本功能和用法](https://www.cnblogs.com/yelao/p/9841810.html)



### Java Instrumentation 的核心方法

```java
/**
 * 为 Instrumentation 注册一个类文件转换器，可以修改读取类文件字节码
 */
void addTransformer(ClassFileTransformer transformer, boolean canRetransform);

/**
 * 对JVM已经加载的类重新触发类加载
 */
void retransformClasses(Class<?>... classes) throws UnmodifiableClassException;

/**
 * 获取当前 JVM 加载的所有类对象
 */
Class[] getAllLoadedClasses()
```

- **ClassFileTransformer** 

  > class 文件转换器,核心方法 transform，将加载的 class 文件流尽心转换为我们自定义的 class
  >
  > 后续 JVM 加载所有类之前都会被这个 transform 方法拦截



### Javaagent 介绍

Javaagent 是一个特殊的 jar 包，它并不能单独启动的，而必须依附于一个 JVM 进程，可以看作是 JVM 的一个寄生插件，使用 Instrumentation 的 API 用来读取和改写当前 JVM 的类文件。



#### Agent 的两种使用方式

- JVM 启动时加载，传入 javaagent 启动参数(执行的是 **premain** 方法)：**java -javaagent:myagent.jar MainClass**

  > public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception

- JVM 启动后 Attach,通过 Attach API 进行加载，在 agent 加载以后执行 **agentmain** 方法

  > public static void agentmain(String agentArgument, Instrumentation instrumentation) throws Exception

> **agentArgument** 是 agent 的启动参数，可以在命令行中设置，如：**java -javaagent:<jarfile>=appId:agent-demo,agentType:singleJar test.jar** ** 中 agentArgument 值为
>
> **“appId:agent-demo,agentType:singleJar”**
>
> **instrumentation ** 是 Instrumentation 的实例，可以通过 addTransformer 方法设置一个 ClassFileTransformer

![premain方式的加载时序图](imgs/premain-add.awebp)



#### Agent 打包

> 为了能够以 javaagent 的方式运行 premain 和 agentmain 方法，我们需要将其打包成 jar 包，并在其中的 MANIFEST.MF 配置文件中，指定 Premain-class 等信息，一个典型的生成好的 MANIFEST.MF 内容如下

```properties
Premain-Class: me.geek01.javaagent.AgentMain
Agent-Class: me.geek01.javaagent.AgentMain
Can-Redefine-Classes: true
Can-Retransform-Classes: true
```

相关 maven 配置：

```xml
<build>
  <finalName>my-javaagent</finalName>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-jar-plugin</artifactId>
      <configuration>
        <archive>
          <manifestEntries>
            <Agent-Class>me.geek01.javaagent.AgentMain</Agent-Class>
            <Premain-Class>me.geek01.javaagent.AgentMain</Premain-Class>
            <Can-Redefine-Classes>true</Can-Redefine-Classes>
            <Can-Retransform-Classes>true</Can-Retransform-Classes>
          </manifestEntries>
        </archive>
      </configuration>
    </plugin>
  </plugins>
</build>
```

[JVM Attach API 示例代码](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Farthur-zhang%2Fjvm-attach-code%2Ftree%2Fmaster%2Fmy-attach-demo)



#### JVM Attach API 的底层原理

> 基于信号和 Unix 域套接字

- **信号**

  > 信号是某事件发生时对进程的通知机制，也被称为“软件中断”。信号可以看做是一种非常轻量级的进程间通信，信号由一个进程发送给另外一个进程，只不过是经由内核作为一个中间人发出，信号最初的目的是用来指定杀死进程的不同方式.
  >
  > 每个信号都有一个唯一的数字标识，从 1 开始，下面是常见的信号量列表

  | 信号名  | 编号 | 描述                                                        |
  | ------- | ---- | ----------------------------------------------------------- |
  | SIGINT  | 2    | 键盘中断信号(Ctrl+C)                                        |
  | SIGQUIT | 3    | 键盘推出信号(Ctrl+/)                                        |
  | SIGKILL | 9    | "必杀"(sure kill)信号，应用程序无法忽略或者捕获，总会被杀死 |
  | SIGTERM | 15   | 终止信号                                                    |

  在 Linux 中，一个前台进程可以使用 Ctrl+C 进行终止，对于后台进程需要使用 kill 加进程号的方式来终止，kill 命令是通过发送信号给目标进程来实现终止进程的功能。默认情况下，kill 命令发送的是编号为 15 的 `SIGTERM` 信号，这个信号可以被进程捕获，选择忽略或正常退出。目标进程如果没有自定义处理这个信号，就会被终止。对于那些忽略 `SIGTERM` 信号的进程，则需要编号为 9 的 SIGKILL 信号强行杀死进程，SIGKILL 信号不能被忽略也不能被捕获和自定义处理。

  > JVM 对 **SIGQUIT 的默认行为是打印所有运行线程的堆栈信息**，在类 Unix 系统中，可以通过使用命令 **kill -3 pid 来发送 SIGQUIT 信号**。

- **Unix 域套接字(Unix Domain Socket)**

  > Unix 域套接字是一个文件,两个进程通过读写这个文件就实现了进程间的信息传递。
  - 与普通套接字的区别
    - Unix 域套接字更加高效，Unix 套接字不用进行协议处理，不需要计算序列号，也不需要发送确认报文，只需要复制数据即可
    - Unix 域套接字是可靠的，不会丢失报文，普通套接字是为不可靠通信设计的
    - Unix 域套接字的代码可以非常简单的修改转为普通套接字
  - [JEP 380: Unix-Domain Socket Channels](https://openjdk.java.net/jeps/380)