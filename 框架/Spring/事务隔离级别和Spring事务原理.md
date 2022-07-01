## 1. 事务隔离级别

- 读未提交(Read Uncommitted) -- 脏读
- 读已提交(Read Committed) -- 不可重复读 -- 同一事务内对同一条数据进行两次读取，返回的数值不一致
- 可重复读(Read Repeatable) -- 同一事务内对同一条数据进行多次读取，返回的数值一致
- 串行化(Serializable) -- 不允许多个事务并行操作 -- 避免了脏读、不可重复读和幻读的发生
  - 幻读：事务A查询一条数据A --> 事务B插入数据B --> 事务A再次查询就有A、B两条数据了



## 2.  Spring 的事务支持和传播特性

### 2.1 Spring 对事务的支持

> Spring 定义了以下 3 个接口来支持事务

- `TransactionDefinition:`**定义了事务的属性(包括传播行为、隔离级别、超时、是否只读等)**

  ```java
  public interface TransactionDefinition {
  
  	int PROPAGATION_REQUIRED = 0;
  
  	int PROPAGATION_SUPPORTS = 1;
  
  	int PROPAGATION_MANDATORY = 2;
  
  	int PROPAGATION_REQUIRES_NEW = 3;
  
  	int PROPAGATION_NOT_SUPPORTED = 4;
  
  	int PROPAGATION_NEVER = 5;
  
  	int PROPAGATION_NESTED = 6;
  
  	int ISOLATION_DEFAULT = -1;
  
  	int ISOLATION_READ_UNCOMMITTED = 1;  // same as java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
  
  	int ISOLATION_READ_COMMITTED = 2;  // same as java.sql.Connection.TRANSACTION_READ_COMMITTED;
  
  	int ISOLATION_REPEATABLE_READ = 4;  // same as java.sql.Connection.TRANSACTION_REPEATABLE_READ;
  
  	int ISOLATION_SERIALIZABLE = 8;  // same as java.sql.Connection.TRANSACTION_SERIALIZABLE;
  
  	int TIMEOUT_DEFAULT = -1;
  
  	default int getPropagationBehavior() {
  		return PROPAGATION_REQUIRED;
  	}
  
  	default int getIsolationLevel() {
  		return ISOLATION_DEFAULT;
  	}
  
  	default int getTimeout() {
  		return TIMEOUT_DEFAULT;
  	}
  
  	default boolean isReadOnly() {
  		return false;
  	}
  
  	@Nullable
  	default String getName() {
  		return null;
  	}
  
  	// Static builder methods
  	static TransactionDefinition withDefaults() {
  		return StaticTransactionDefinition.INSTANCE;
  	}
  }
  
  ```

  

- `TransactionStatus:`**描述了事务具体的运行状态**

  ```java
  public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {
  
  	boolean hasSavepoint();
  
  	@Override
  	void flush();
  }
  
  public interface TransactionExecution {
  
  	boolean isNewTransaction();
  
  	void setRollbackOnly();
  
  	boolean isRollbackOnly();
  
  	boolean isCompleted();
  }
  
  public interface SavepointManager {
  
  	Object createSavepoint() throws TransactionException;
  
  	void rollbackToSavepoint(Object savepoint) throws TransactionException;
  
  	void releaseSavepoint(Object savepoint) throws TransactionException;
  
  }
  ```

  - 继承自 `SavepointManager` 和 `TransactionExecution`

  - `boolean isNewTransaction():`判断当前事务是否时新事务，如果 false，说明当前事务已存在，或者该操作没在事务环境中

  - `boolean hasSavePoint():`判断当前事务是否有保存点

  - `void setRollBackOnly():`设置为 RollBackOnly 模式，事务管理器接收到这个指令后会回滚事务

  - `boolean isRollBackOnly()`

  - `void flush():`刷新修改的数据到数据库

  - `boolean isCompleted():`判断当前事务是否结束

  - `Object createSavePoint():`创建保存点

  - `void rollBackToSavePoint(Object savepoint):`回滚到保存点，回滚之后保存的自动释放

  - `void releaseSavepoint(Object savepoint):`释放指定保存点

    

- `PlatformTransactionManager:`**事务管理器**

  ```java
  public interface PlatformTransactionManager extends TransactionManager {
  	// 传入一个 TransactionDefinition 创建一个事务，返回 TransactionStatus 描述事务具体运行状态
  	TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
  			throws TransactionException;
  	// 根据事务运行状态进行提交操作，如果是 rollback-only 则回滚事务
  	void commit(TransactionStatus status) throws TransactionException;
      // 回滚操作
  	void rollback(TransactionStatus status) throws TransactionException;
  }
  ```
  
  

### 2.2 Spring 事务的声明方式

- `@Transactioanl`

  - 可以配置的属性：propagation,isolation,read-only,timeout,rollbackFor,noRollbackFor,rollbackForClassName,noRollbackForClassName

- XML 配置文件的方式

  

### 2.3 Spring 事务的传播特性

> `定义:`**事务中嵌套其他事务，事务与被嵌套事务之间是如何相互影响、如何执行的**

```java
class ServiceA {
    @Autowired
    ServiceB serviceB;
    
    @Transactional
    public void methodA() {
        //数据库操作
        db.insert(new A());
        // 调用 B
        try {
            serviceB.methodB();
        } catch(Exception e) {
            // 打印异常日志
        }
    }
}

class ServiceB {
    @Transactional(propagation = PROPAGATION_REQUIRES_NEW)
    public void methodB() {
        // 数据库操作
    }
}
```

**以上情况事务的传播行为分为 7 种，主要集中在被嵌套的事务 methodB() 这里：**

- `PROPAGATION_REQUIRED`: 最常见，**methodB 不开启独立事务**，跟随 A，任何一个报错都导致整个事务的回滚
- `PROPAGATION_SUPPORTS`: methodB **跟随 methodA，A 开启事务就加入，A 没有事务就整个都没有**
- `PROPAGATION_MANDATORY`：**必须被一个开启了事务的方法来调用自己，否则报错**
- `PROPAGATION_REQUIRE_NEW`:  methodB 开启**独立事务**，**A 在 B 事务执行完再执行**。A 报错 B 不会受影响，B 报错 A 选择性提交或者回滚
- `PROPAGATION_NOT_SUPPORTED`：methodB **不支持事务**，**A 执行到 B 自己的事务就挂起来了，methodB 报错不会让 A 回滚**
- `PROPAGATION_NEVER`：**不支持事务，A 开启事务了此时调用 methodB 会报错**
- `PROPAGATION_NESTED`：methodB 开启一个**子事务**，如果回滚就会**滚到 methodB  开启子事务的 save point**



### 3. Spring 事务失效的几种情况

- 数据库引擎不支持事务
- 没有被 Spring 管理，不是 Spring Bean
- 修饰的**方法不是 public 的**
- 自身调用问题(类的方法调用了自身类的其他方法)
- 数据源没有配置事务管理器
- **不支持事务  Propagation.NOT_SUPPORTED**
- catch 异常未抛出
- 抛出的异常类型错误(默认回滚的是 RuntimeException)