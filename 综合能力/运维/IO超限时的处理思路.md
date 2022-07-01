
### 磁盘IO超限，我们能做什么
程序在运行时，会对磁盘进行大量的读写操作，长时间的磁盘读写会对硬盘造成损耗，长时间的高IO运行会对平台的业务造成不可预知的影响。数据库磁盘缓存\消息队列各类文件(日志、图片...)，软件平台操作变慢视频取流失败登录变慢页面查询超时告警延时...IO引起的问题现象用户为了保证服务器的正常运行，运管中心会对IO状态进行监控，当告警页面出现“服务器磁盘IO超限”的告警时，需要引起我们的重视，但不需要恐慌，我们可以结合软硬件、项目容量、业务使用峰值等各种因素来共同判断当前指标是否正常。下面，我们将从多维度向大家做一个分享。

### 硬件能力

在硬件上，机械盘7.2K的企业级SATA硬盘I/O能力约为70 ~ 100M左右（根据实际数据块大小），10K的企业级SAS硬盘I/O能力约为120 ~ 150M左右（根据实际数据块大小），企业级SSD的I/O能力一般大于500M，我们可以用工具测试下当前硬盘的实际I/O能力有多少，如果一台服务器，开机都要5分钟，那优先考虑从硬件层面进行优化，一般来说，**从硬件层面做提升能看到最直观的改善效果。**

### 操作系统层面
操作系统也存在大量的读写pagefile.sys（页文件）的情况，该文件其实是虚拟内存文件。目前发现有的操作系统windows defender会大量占用虚拟内存的问题，并且服务器上会不时弹出如下报错，大致意思是内存已满，但是从任务管理器来看内存并没有满。这种情况下关闭windows defender即可，具体操作参考《关闭windows-defender的操作方法.docx》。关闭windows-defender的操作方法.docx软件层面运管中心的状态监控页面，会实时监控当前IO占用top5的进程，我们可以根据进程名来逐一分析一些常见的进程并进行优化。

日志组件logservice日志组件承担着整个平台所有日志的采集与统计工作，包括操作日志与系统运行日志，业务量越多数据量也越多，数据量大的话势必带来大量的IO读写，最有效的解决方法就是logservice分布式部署，大型项目一般也不会只有一台服务器，在光盘安装时就给出了明确的提示，日志组件建议分布式部署。Logservice是服务端，而每台服务器上都还有一个客户端logagent，如果发现io读写高的进程中还有一个hik.opsmgr.logagent.exe的话，可以把“系统日志采集”的开关关掉，只采集操作日志来降低IO操作。

缓存组件redis当IO高的进程显示为redis-server.exe时，表示redis组件当前读写较频繁。在综合安防场景下，并未用到redis的持久化机制，因此可以通过修改redis的持久化策略，使redis不在实时写缓存数据到aof文件，减少io资源占用。修改方法为，进入组件目录，\web\components\rediswin64.1\conf\redis.windows-service.conf，将文件中内容，appendonly yes 改为no，aof-use-rdb-preamble yes 改为no（iSC 1.2.0不需要配置这个），重启redis。
数据库组件postgresqlpg组件本身不占用IO，各组件在使用pg组件进行数据存储时会进行磁盘读写，表现为postgres.exe进程会占用IO。解决此类问题，可以有多种方式，以下介绍几个常用的方式。通过pid定位使用pg进程的组件1、通过监控页面，我们可以获取到postgres.exe进程对应的pid；2、通过数据库连接工具，使用postgres用户登录pg组件，连接库postgres并打开查询窗口；3、根据pid查对应query，我们能定位到疑似有问题的组件，给进一步排查指明方向。按照数据库id查select   *   from pg_stat_activity where pid = PID;Windows：PID为对应postgres.exe的进程PID；Linux：通过top命令，查看postgres进程对应的PID更多监控pg的语句请参考以下连接

https://wiki.hikvision.com.cn/pages/viewpage.action?pageId=143752598pg缓存策略该策略能够显著的降低IO读写频率。shared_buffers是一个8KB的数组，postgres在从磁盘中查询数据前，会先查找shared_buffers的页，如果命中，就直接返回，避免从磁盘查询，因此通过提高该参数值提高缓存，降低从磁盘读数据频率。具体操作步骤如下：1、打开pg组件的配置文件\web\components\postgresql96win64.1\data\postgresql.conf；2、修改参数shared_buffers =2014MB，如果pg为单独服务器部署，可设置为内存的1/4；3、改完之后，执行目录重新加载配置文件；windows：进入目录\web\components\postgresql96win64.1\bin执行命令：pg_ctl reload -D “安装目录\web\components\postgresql96win64.1\data”Linux：进入目录/web/components/postgresql96linux64.1/bin执行命令：./bin/pg_ctl reload -D /安装目录/web/components/postgresql96linux64.1/data分布式部署pg各组件分摊使用不同的pg组件，不失为另一个解决方法。项目前期梳理好部署方案，可以在不同服务器上都安装一个pg数据库，按业务需要，不同的组件使用不同数据库，避免单点pg造成故障，来保证项目的稳定运行。

迁移数据目录若项目已经完成部署并且运行一段时间了，重装组件可能有数据丢失的风险的话，可以单独将数据库data目录迁移到高性能的硬盘上，以提高IO读写能力，操作方法见《linux单独数据迁移方法.docx》。linux单独数据迁移方法.docx目录服务Ldap如果运管监控页面显示的是一个slapd.exe进程IO较高的话，表示ldap占用了较多的磁盘读写，有单独ssd硬盘的话，可以将数据迁移到ssd硬盘上，操作方法见《ldap数据迁移到ssd操作步骤.docx》。ldap数据迁移到ssd操作步骤.docx总结IO优化是一个长期的过程，需要销售、研发、交付共同努力，才能解决问题，我们不难看出，好的部署方案能做到事半功倍的效果，详细可以阅读《分布式部署，产品交付的“救星”》。