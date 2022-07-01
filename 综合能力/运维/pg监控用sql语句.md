### [postgresql] 监控用的sql语句

	1. 获取连接数
	select count(*) from pg_stat_activity

	2. 查询sql执行时间
	SELECT query, calls, total_time, (total_time/calls) as average ,rows, 100.0 * shared_blks_hit /nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent FROM pg_stat_statements ORDER BY average DESC LIMIT 10;

	查一段时间后，可以复位，重新统计

	3. 复位统计信息
	select pg_stat_statements_reset() ;

 	4. 获取最大连接数
 	show max_connections

	5. 获取SQL执行总时间
	select sum(calls) as count,sum(total_time) as total_time from pg_stat_statements

	6. 获取数据库的总的行数执行速度
	SELECT sum(xact_commit) + sum(xact_rollback),sum(tup_inserted)+ sum(tup_updated)+ sum(tup_deleted),sum(tup_fetched)+ sum(tup_returned),sum(blks_read) FROM pg_stat_database

**获取数据库的信息**

	1. 数据名 数据库大小
	select pg_database.datname, pg_database_size(pg_database.datname) from pg_database
	select datname,count(*) as count from pg_stat_activity group by datname
	select datname,xact_rollback,deadlocks from pg_stat_database
	select datname,confl_lock,confl_bufferpin,confl_deadlock from pg_stat_database_conflicts


	2. 查看数据库表大小（单位为M）
	select table_schema,TABLE_NAME,reltuples,pg_size_pretty(pg_total_relation_size('"' || table_schema || '"."' || table_name || '"'))from pg_class,information_schema.tables where relname=TABLE_NAME ORDER BY reltuples desc limit 20

	查看指定的pid对应的数据库信息，登录数据之后，查询pg_stat_activity 	
	postgres=# select * from pg_stat_activity
    
	或者搜索指定的pid:  
	postgres=# select * from pg_stat_activity where pid = 22704; 
    
	查看数据库的创建时间

	使用SQL查询所有数据库的创建时间，取modification

	select
		datname,
		(pg_stat_file(format('%s/%s/PG_VERSION',
		case
		when spcname='pg_default' then 'base'
		else 'pg_tblspc/'||t2.oid||'/PG_11_201804061/'
		end,
		t1.oid))).*
		from
		pg_database t1,
		pg_tablespace t2
		where t1.dattablespace=t2.oid;

更多监控信息参考这个页面：
https://www.postgresql.org/docs/9.2/monitoring-stats.html