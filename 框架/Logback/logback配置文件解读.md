### 示例

```
<!-- scan="true", 不要修改。设为true支持动态调整打印级别（不需重启程序）,已提供工具统一改级别，若设false则无效 -->
<configuration scan="true" scanPeriod="60 seconds" debug="false">

    <!--从 Spring Environment 中获取组件标识和段标识-->
    <springProperty scope="context" name="component.id" source="component.id"/>
    <springProperty scope="context" name="component.segment.id" source="component.segment.id"/>

    <!-- 组件封装时, 段的log.path也要配成'./logs/组件标识', 否则采集不到日志 -->
    <property name="LOG_HOME" value="${user.dir}/logs/${component.id}/" />

    <!-- 开发环境配置 -->
    <springProfile name="dev">
        <!-- 控制台Appender, 不要修改 -->
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %level ${component.id}.${component.segment.id} [%thread] [%logger{50}:%line] %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

        <root level="DEBUG">
            <appender-ref ref="STDOUT"/>
        </root>
    </springProfile>

    <!--非开发环境配置-->
    <springProfile name="!dev">
        <!-- 操作日志Appender, 不要修改 -->
        <appender name="BUS" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <File>${LOG_HOME}/${component.id}.${component.segment.id}.business.log</File>
            <rollingPolicy class="logback.rolling.log.gather.TimeBasedBackUpRollingPolicy">
                <FileNamePattern>${LOG_HOME}/${component.id}.${component.segment.id}.business.log.%i.zip</FileNamePattern>
                <MinIndex>1</MinIndex>
                <!--最多十个文件 -->
                <MaxIndex>10</MaxIndex>
            </rollingPolicy>
            <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <MaxFileSize>25MB</MaxFileSize>
            </triggeringPolicy>
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} - %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

        <logger name="HikBusLog" level="INFO" additivity="false">
            <appender-ref ref="BUS" />
        </logger>
        <!-- 操作日志Appender,   结束 -->

        <!-- 追踪日志Appender, 不要修改 -->
        <appender name="DTS" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <File>${LOG_HOME}/${component.id}.${component.segment.id}.dts.log</File>
            <rollingPolicy class="logback.rolling.log.gather.TimeBasedBackUpRollingPolicy">
                <!--日志文件输出的文件名 -->
                <FileNamePattern>${LOG_HOME}/${component.id}.${component.segment.id}.dts.log.%i.zip</FileNamePattern>
                <MinIndex>1</MinIndex>
                <!--最多十个文件 -->
                <MaxIndex>10</MaxIndex>
            </rollingPolicy>
            <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <MaxFileSize>25MB</MaxFileSize>
            </triggeringPolicy>
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} [%thread] [%logger{50}:%line] - %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

		<!-- additivity="false" 表示子Logger只会在自己的appender中输出，不会叠加到root的appender中-->
        <logger name="com.hikvision.swdf.impl.TraceRecordGenerator" level="INFO" additivity="false">
            <appender-ref ref="DTS" />
        </logger>
        <!-- 追踪日志Appender,  结束 -->

        <!-- 系统日志文件打印配置     开始 -->
        <logger name="com.hikvision.sso" level="WARN" additivity="false"/>

        <!-- 所有级别(TRACE及以上)的日志输出配置, 所有级别打印在*.debug.log中, 文件名用debug表示调试用, 便于理解 -->
        <appender name="FILE-debug" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>TRACE</level>
            </filter>
            <File>${LOG_HOME}/${component.id}.${component.segment.id}.debug.log</File>
            <rollingPolicy class="logback.rolling.log.gather.TimeBasedBackUpRollingPolicy">
                <FileNamePattern>${LOG_HOME}/${component.id}.${component.segment.id}.debug.log.%i.zip
                </FileNamePattern>
                <MinIndex>1</MinIndex>
                <MaxIndex>10</MaxIndex>
            </rollingPolicy>
            <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <!-- debug文件大小设置为100M, 再压缩 -->
                <MaxFileSize>100MB</MaxFileSize>
            </triggeringPolicy>
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %level ${component.id}.${component.segment.id} [%thread] [%logger{50}:%line] %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

        <!-- 错误日志输出配置, error及以上日志单独再打印一个*.error.log, 防止重要信息被覆盖-->
        <appender name="FILE-error" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>ERROR</level>
            </filter>
            <File>${LOG_HOME}/${component.id}.${component.segment.id}.error.log</File>
            <rollingPolicy class="logback.rolling.log.gather.TimeBasedBackUpRollingPolicy">
                <FileNamePattern>${LOG_HOME}/${component.id}.${component.segment.id}.error.log.%i.zip
                </FileNamePattern>
                <MinIndex>1</MinIndex>
                <MaxIndex>10</MaxIndex>
            </rollingPolicy>
            <triggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <MaxFileSize>25MB</MaxFileSize>
            </triggeringPolicy>
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %level ${component.id}.${component.segment.id} [%thread] [%logger{50}:%line] %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

        <!-- SPRING等框架类代码日志打印, 输出到OTHER文件中, 出厂默认WARN以上 -->
        <!-- 解析不了和入库失败的日志内容 -->
        <appender name="OTHER" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>WARN</level>
            </filter>
            <File>${LOG_HOME}/${component.id}.${component.segment.id}.other.log</File>
            <rollingPolicy class="logback.rolling.log.gather.TimeBasedBackUpRollingPolicy">
                <FileNamePattern>${LOG_HOME}/${component.id}.${component.segment.id}.other.log.%i.zip</FileNamePattern>
                <MinIndex>1</MinIndex>
                <!--最多十个文件 -->
                <MaxIndex>10</MaxIndex>
            </rollingPolicy>
            <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <MaxFileSize>25MB</MaxFileSize>
            </triggeringPolicy>
            <encoder class="logback.encoder.LazyTimeZonePatternLayoutEncoder">
                <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %level ${component.id}.${component.segment.id} [%thread] [%logger{50}:%line] - %msg%n</pattern>
                <charset>UTF-8</charset>
            </encoder>
        </appender>

        <logger name="org.apache.zookeeper.ClientCnxn" level="ERROR" />
        <logger name="org.springframework.boot" level="WARN" />
        <logger name="org.springframework.boot.web.filter" level="ERROR" />

        <!-- 出厂默认输出级别INFO, 排查问题时, 可以通过工具切换为TRACE -->
        <logger name="com.hikvision" level="INFO" additivity="false">
            <appender-ref ref="FILE-debug" />
            <appender-ref ref="FILE-error" />
        </logger>

        <root level="WARN">
            <appender-ref ref="OTHER" />
        </root>
    </springProfile>

</configuration>
```
### 常见占位符含义

####1. %X

    %X 用于输出和当前线程相关的 MDC （MDC中设置的 key/value）
    例： System.setProperty("logging.pattern.level", "%5p logservice.%X{logservice:-log}") --> %X{logservice:-log} 表示key为logservice的变量，log为没有时的默认值
    // 以下代码来自 spring-cloud-sleuth 的 TraceEnvironmentPostProcessor
    map.put("logging.pattern.level", "%5p [${spring.zipkin.service.name:${spring.application.name:-}},%X{X-B3-TraceId:-},%X{X-B3-SpanId:-},%X{X-Span-Export:-}]");

####1. ${}

    ${}用于获取系统变量的值，如${user.dir}获取用户目录值
    ${}也可以配合Spring Environment变量使用：
         <springProperty scope="context" name="自定义变量名" source="spring environment中的key名称"/>
         使用： ${自定义变量名}