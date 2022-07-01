### 1. 类签名

```java
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable
```

### 2. 父类 AOS（AbstractOwnableSynchronizer）

```java
# 定义了独占锁的线程变量及getter/setter方法
private transient Thread exclusiveOwnerThread
```

