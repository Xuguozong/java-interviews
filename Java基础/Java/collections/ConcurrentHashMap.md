### 1. [key points](https://www.geeksforgeeks.org/concurrenthashmap-in-java/)

- 底层数据结构是 [HashTable](https://www.geeksforgeeks.org/hashtable-in-java/)
- 线程安全的
- 读操作无需锁上整个类实例
- 根据并发水平(concurrency level)分成多个段(segments)
- 默认并发水平是 16
- 对于正在进行修改操作的 segment 数据的读取会加锁 locking
- key 或 value 都不允许为 null