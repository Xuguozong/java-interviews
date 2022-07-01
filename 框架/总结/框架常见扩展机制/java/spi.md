## Java SPI 扩展机制

**SPI全称Service Provider Interface，是Java提供的一套用来被第三方实现或者扩展的API，它可以用来启用框架扩展和替换组件。Java SPI 实际上是“基于接口的编程＋策略模式＋配置文件”组合实现的动态加载机制。**

#### 使用
	1、当服务提供者提供了接口的一种具体实现后，在jar包的META-INF/services目录下创建一个以“接口全限定名”为命名的文件，内容为实现类的全限定名；
	2、接口实现类所在的jar包放在主程序的classpath中；
	3、主程序通过java.util.ServiceLoder动态装载实现模块，它通过扫描META-INF/services目录下的配置文件找到实现类的全限定名，把类加载到JVM；
	4、SPI的实现类必须携带一个不带参数的构造方法；

    示例：
    	- resources
    		- META-INF
    			- services
                	- ch.qos.logback.classic.spi.Configurator
        其中ch.qos.logback.classic.spi.Configurator文件的内容为其实现类：com.example.zipkinservice4.logback.spi.SelfConfigurator

以上示例就完成了logback配置文件的[自定义配置解析器](https://logback.qos.ch/manual/configuration.html)