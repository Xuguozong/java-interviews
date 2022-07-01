1. 查询 MySQL 版本

   ```shell
   # docker exec container-mysql mysqld --version
   mysqld  Ver 5.7.34 for Linux on x86_64 (MySQL Community Server (GPL))
   ```

2. 查看配置文件

   ```shell
   # docker exec container-mysql cat /etc/mysql/mysql.conf.d/mysqld.cnf
   
   [mysqld]
   pid-file        = /var/run/mysqld/mysqld.pid
   socket          = /var/run/mysqld/mysqld.sock
   datadir         = /var/lib/mysql
   #log-error      = /var/log/mysql/error.log
   # By default we only accept connections from localhost
   #bind-address   = 127.0.0.1
   # Disabling symbolic-links is recommended to prevent assorted security risks
   symbolic-links=0
   ```

3. 拷贝出配置文件

   ```shell
   # docker cp container-mysql:/etc/mysql/mysql.conf.d/mysqld.cnf mysqld.cnf
   ```

4. 添加慢查询相关配置

   ```shell
   # vim mysqld.cnf
   ------------------
   # Slow query settings:
   slow_query_log=1
   slow_query_log_file=/var/log/mysql/slow.log
   long_query_time=0.1
   
   character-set-server=utf8
   
   [client]
   default-character-set=utf8
   ```

5. 拷贝回容器

   ```shell
   # docker cp mysqld.cnf container-mysql/etc/mysql/mysql.conf.d/mysqld.cnf
   ```

6. 重启容器

   ```shell
   # docker restart container-mysql
   ```

7. 查看慢查询日志

   ```shell
   # docker exec container-mysql cat /var/log/mysql/slow.log
   ```

