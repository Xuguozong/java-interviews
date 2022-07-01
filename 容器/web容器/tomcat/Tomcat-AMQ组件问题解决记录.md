## Tomcat、AMQ组件问题解决记录

序号 | 组件 | 描述 | 环境 | 是否解决 | 原因 | 解决方法 | 日期
- | - | :-: | -: | :-: | :-: |
1| AMQ | 一直重启 | win server 2016 | 是 | 磁盘占满，修改配置信息无法落盘，重启读取了空的配置文件 | 清理磁盘空间，复制新配置文件 | 2018-04-24 
2| Tomcat | dump | win server 2016 |否 | war包部署失败，一直重新部署 | 限制重启war包次数 | 2018-04-24
3| Tomcat | 无法停止 | win server 2016 | 否 | webapps下放入不能启动的war包，停止不来tomcat | 需要手动停止进程 | 2018-04-26
4| Tomcat | 配置参数修改需重启两次生效 | linux |是 | 修改的参数先到config.properties,再通过InstallTomcat写到tomcat_script启动 | 在tomcat_service中添加ExecStartPre字段，在执行tomcat_script之前执行com_start.sh |2018-05-02
5| AMQ | 有些环境安装检测失败 | linux | 是 | AMQ安装成系统服务后延迟十几秒才开放端口 | 修改installdetect.sh,将检测amq连接改为检测系统服务作为安装成功的标志 | 2018-04-27
6| 所有组件 | agent读取不到linux版本组件日志 | linux | 是 | linux版本日志命名与win版本相同，agent读取不了 | 修改日志配置 | 2018-04-27