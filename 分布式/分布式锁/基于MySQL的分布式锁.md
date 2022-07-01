### 1. 基于去重表实现

`去重表 sql:`

```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for t_lock
-- ----------------------------
DROP TABLE IF EXISTS `t_lock`;
CREATE TABLE `t_lock`  (
  `lock_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加锁信息',
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
```

`锁注解:`

```java
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ExclusiveLock {
    /** 锁的名称 */
    String lockName();
}
```

`切面:`

```java
/** 通过数据库唯一性主键约束实现的独占锁，同一时刻只有一个定时任务能获取锁并成功执行，其他定时任务不执行
 * 2021-9-14 更新
 *      1. 去除显示删除数据库锁记录逻辑
 *      2. 修改为隐式锁失效逻辑(通过锁记录生成的时间)
 *      3. 注意事项：最小粒度为分钟，一分钟内多次执行的定时任务只会执行一次
 *      4. 参考：接口幂等问题的去重表方案
 */
@Slf4j
@Aspect
@Component
public class ExclusiveLockAspect {

    @Autowired
    TLockMapper lockMapper;

    @Pointcut("@annotation(com.afs.app.common.annotation.ExclusiveLock)")
    public void exclusiveLock() {}

    @Around("exclusiveLock()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        ExclusiveLock lock = method.getAnnotation(ExclusiveLock.class);
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        // 锁失效时间默认包含在这里
        String lockName = lock.lockName() + "-" + methodName + "-" + DateUtil.date().toString("yyyy-MM-dd:HH:mm");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        Example example = new Example(ELock.class);
        example.createCriteria().andEqualTo("lockName", lockName);
        ELock tLock = lockMapper.selectOneByExample(example);
        // 如果有记录表示已经被其他客户端执行了一次
        if (ObjectUtil.isNotNull(tLock)) return null;
        tLock = new ELock(lockName, hostAddress);
        lockMapper.insert(tLock);
        // 执行主流程
        Object proceed;
        try {
            // 执行主流程
            proceed = point.proceed();
        } catch(Exception e) {
            // 只执行一次任务，失败只做记录
            log.error("执行主流程发生异常，方法:[{}],错误信息:{}",
                    methodName, e.getMessage());
            return null;
        }
        //
        return proceed;
    }

}
```

`可以继续完善的地方:`

```
1. 在 @ExclusiveLock 添加方法指定执行的时间粒度
2. 目前方法执行失败只做了记录，可以根据实际项目需要决定是否加入重试机制
```

