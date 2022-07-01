[TOC]

## Spring面试题整理

### 1. 使用Spring框架带来的好处和缺点
**好处**：

	1） 轻量级 IoC 容器相比EJB容器更轻量
	2） 集成方便，开发便捷，很多主流框架已经集成进来了，开箱即用
	3） AOP 支持面向切面编程，更方便地将应用逻辑和系统服务区分开来
	4） DI 是的构造器、JavaBean、properties文件中的依赖关系一目了然
	5） 测试便捷
	6） web框架简洁易用
	7） 事务管理便捷
	8） 异常处理，将特定框架的异常统一转换为一致的、Unchecked异常
	9） 群众基础大，遇到问题解决成本低

**缺点**：

	封装的太好了导致细节都被隐藏，从Java工程师变成Spring工程师，眼光很容易局限在Spring生态圈

****

### 2. Spring框架中用到的设计模式
    1） 代理模式 -- AOP 和 remoting
    2） 单例模式 -- Bean 默认单例
    3） 模板方法 -- RestTemplate、JmsTemplate、JdbcTemplate
    4） 前端控制器 -- 使用 DispatcherServlet 来分发请求
    5） 依赖注入 -- 贯穿于 BeanFactory、ApplicationContext 接口的核心理念
    6） 工厂模式 -- BeanFactory 用来创建对象的实例

### 3. 什么是Spring IoC容器
**Spring框架的核心，容器创建Bean对象，将它们装配在一起，配置并管理它们的完整生命周期**

- Spring 容器使用依赖注入来管理bean对象
- 容器通过读取提供的配置元数据 Bean Definition 来接受对象进行实例化，配置和组装
- 该配置元数据Bean Definition可通过XML、Java注解或Java Config代码提供
- 通过引入案例：比如实现一个 Redis、JDBC 的客户端库，如何装配、starter、配置(XML以及自动配置等)

### 4. 什么是依赖注入
在依赖注入中，你不必主动、手动创建对象，但必须描述如何创建它们

- 不是直接在代码中将组件和服务连接在一起，而是描述配置文件中哪些组件需要哪些服务
- 然后，再由IoC容器将它们装配在一起

### 5. IoC与DI的区别
IoC 是更宽泛的概念，DI 是更具体的实现

### 6. 依赖注入的几种方式
- 接口注入
- 构造函数注入
- setter注入
spring中仅使用构造函数和setter注入这两种方式

|构造函数注入|setter注入|
|--------|--------|
|没有部分注入|有部分注入|
|不会覆盖setter属性|会覆盖|
|任意修改会创建一个新实例|不会创建|
|适用设置很多属性|适用于设置少量属性|
|优先依赖的优先注入|没有顺序|

### 7. Spring中有几种IoC容器
- BeanFactory
- ApplicationContext
    扩展了BeanFactory接口，在其基础上扩展的功能：
    MessageSource:管理message，实现国际化等功能
    ApplicationEventPublisher:事件发布
    ResourcePatternResolver:多资源加载
    EnvironmentCapable:系统Environment（profile+Properties）相关
    Lifecycle:管理生命周期
    Closable:关闭、释放资源
    InitializingBean:自定义初始化
    BeanNameAware:设置BeanName的Aware接口
    另外，它会自动初始化非懒加载的Bean对象

### 8. 介绍下常用的BeanFactory容器
- XmlBeanFactory 根据xml文件中定义的内容，创建子相应的bean

### 9. 介绍下常用的ApplicationContext容器
- ClassPathXmlApplicationContext
    从 classpath 的xml 配置文件中读取上下文并生成上下文定义
- FileSystemXmlApplicationContext
    由文件系统中的xml配置文件读取上下文
- XmlWebApplicationContext
    由web应用的xml文件读取上下文
- ConfigServletServerApplicationContext
    springboot使用的

### 10. 列举一些IoC的好处
- 减少代码量
- 松耦合，少侵入
- 支持即时的实例化和延迟加载bean对象
- 易于测试

### 11. Spring IoC的实现机制
- 工厂模式和反射机制

### 12. Spring的事件类型
- Spring 的 ApplicationContext 提供了支持事件和代码中监听器的功能
- 如果一个 Bean 实现了 ApplicationListener 接口，当一个ApplicationEvent 被发布以后，Bean 会自动被通知
- 五种标准事件
    1. ContextRefreshedEvent(上下文更新事件)：上下文初始化或更新时发布，也可以在ConfigurableApplicationContext#refresh()中被调用
    2. ContextStartedEvent（上下文开始事件）：ConfigurableApplicationContext#start()方法调用
    3. ContextStoppedEvent（上下文停止事件）：ConfigurableApplicationContext#stop()方法调用
    4. ContextClosedEvent （上下文关闭事件）：ApplicationContext被关闭时触发
    5. RequestHandledEvent（请求处理事件）：HTTP请求触发
- 自定义事件
    1. 继承 ApplicationEvent 自定义事件
    2. 实现 ApplicationListener 自定义事件监听器
    3. 通过 ApplicationContext 接口的 #publishEvent(Object event) 方法来发布自定义事件

### 13. 什么是Spring Bean

### 14. Spring有哪些配置方式

### 15. Spring支持几种Bean Scope

### 16. Spring Bean在容器的生命周期是什么样的

### 17. 什么是Spring的内部Bean

### 18. 什么是Spring装配

### 19. 解释延迟加载

### 20. Spring框架中的单例Bean是线程安全的嘛

### 21. Spring Bean怎么解决循环依赖的问题

### 22. Spring 事务失效的情形

- 没有被 Spring 容器管理
- 事务方法不是 public 的
- 类方法自身调用自身方法，被调用方法事务失效
- 数据源没有配置事务管理器
- 事务传播级别选择
- 事务中出现异常
- rollbackFor 定义了错误的异常类型