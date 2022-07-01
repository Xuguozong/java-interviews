关键字：

monitorenter 指令

monitorexit 指令

运行时常量池  [`method_info`](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6) ACC_SYNCHRONIZED flag

普通方法上是锁实例对象

静态方法上是锁Class对象

OS 层面，mutex 锁

对象头结构，持有偏向锁线程ID，锁升级降级机智

可重入锁

[禁用/废弃偏向锁-JEP374-JDK15](https://openjdk.java.net/jeps/374)

