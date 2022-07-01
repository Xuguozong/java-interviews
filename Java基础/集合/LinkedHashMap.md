### 总体特性

- 继承自 HashMap
- 通过内部链表节点记录了元素的插入顺序



### 关键方法

#### 1. put 元素后添加链表节点

```java
// 此方法重写了 HashMap 的 afterNodeInsertion
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    if (evict && (first = head) != null && removeEldestEntry(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}
```

