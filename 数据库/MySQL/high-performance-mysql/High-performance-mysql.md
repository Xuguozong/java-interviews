# High Performance MySQL Proven Strategies for Operating at Scale, 4th Edition - 笔记





## Performance Schema

![performance-schema-overall](imgs\performance-schema-overall.png)

> Performance Schema 提供了 MySQL 运行时的一些底层的数据指标



### **instrument** 

> 定义了可以获取到的指标类型

- 所有类型存储在 **performance_schema.setup_instruments** 表中
- 例如：
  - statement/sql/select
  - wait/synch/mutex/innodb/autoinc_mutex



### **consumer** 

>  定义了将这些可以获取到的指标数据存储在哪(a consumer is the destination where an instrument sends its information)

#### 分类

- 当前和历史数据(Current and Historical data)

  - - - 底层的阻塞，如获取 mutex
    - events_statements
      - sql 语句层面
    - events_stages
      - Profile information, such as creating temporary tables or sending data
    - events_transactions
      - 事务相关

- 汇总表和摘要信息(Summary and Digest)

  - 汇总表

    - 包含 summary 名称的表，如： memory_summary_by_thread_by_event_name

  - 摘要

    - 将类似 sql 摘要到一处，如：

    - ```sql
      SELECT user,birthdate FROM users WHERE user_id=19;
      SELECT user,birthdate FROM users WHERE user_id=13;
      SELECT user,birthdate FROM users WHERE user_id=27;
      ```

    - 摘要成：

      ```sql
      SELECT user,birthdate FROM users WHERE user_id=?
      ```

- 实例信息(Instances)

  - 实例信息，如 
  - 文件实例(file_instances表)
  - socket 实例(socket_instances表)

- 配置(Setup)

  - 运行时的一些配置

- 其他表

  - 不遵循上述命名规则的其他表
  - 如：**metadata_locks** table holds data about metadata locks