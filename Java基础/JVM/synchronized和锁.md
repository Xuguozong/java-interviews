## synchronized关键字的使用和底层原理

### synchronized 的使用

1. 实例方法上

   ```java
   /** 锁为当前实例 */
   public synchronized void instanceLock() {
   	// code
   }
   ```

   

2. 类方法上

   ```java
   /** 锁为当前 Class 对象 */
   public static synchronized void classLock() {
   	// code
   }
   ```

   

3. 代码块

   ```java
   /** 锁为括号里的对象 */
   public void blockLock() {
   	Object lock = new Object();
   	synchronized(lock) {
   		// code
   	}
   }
   ```

   

## 锁升级流程



> `参考`

[关于synchronized关键字-S.L's Blog](https://elsef.com/2019/11/24/%E5%85%B3%E4%BA%8Esynchronized%E5%85%B3%E9%94%AE%E5%AD%97/)

[monitorenter和monitorexit指令](https://docs.oracle.com/javase/specs/jvms/se6/html/Instructions2.doc9.html)