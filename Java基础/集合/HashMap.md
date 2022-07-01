### 总体特性

- 大体与 HashTable 相同，除了可以存储 null 值和非线程安全。另外它是无序的。
- get() 和 put() 方法都是常量时间的操作（在hash函数能均匀分布的情况下）
  如果要保证遍历时的性能，不要把初始容量设置的过高（或者负载因子设置太低）
- capacity 是哈希表中的桶数量，load factor 是扩容时的负载因子
  扩容时会发生 rehash 操作（内部数据结构重建，翻倍扩容）
- 0.75 的负载因子在时间和空间消耗上提供很好的平衡，
  大一点的话会增加空间利用率但会增加元素查找及插入时间
  实际 entry 的数量和负载因子以及初始容量和实际关系决定的 rehash 操作的频次
- 哈希冲突严重的 key 会影响性能，如果 key 是 Comparable 的子类，
  可以使用 key 之间的比较顺序
- 多线程下不安全
- 返回的迭代器都是 fail-fast 的,fail-fast 的迭代器应当只在检测 bug 的情况下使用

### 关键属性

- **`DEFAULT_INITIAL_CAPACITY = 1 << 4`**
  - 默认初始容量 16
- **`MAXIMUM_CAPACITY = 1 << 30`**
  - 最大容量 1 << 30
- **`DEFAULT_LOAD_FACTOR = 0.75f`**
  - 默认负载因子
- **`TREEIFY_THRESHOLD = 8`**
  - 由链表转换为树的阈值，至少为8
- **`UNTREEIFY_THRESHOLD = 6`**
  - 树转换为链表的阈值，比 TREEIFY_THRESHOLD(8)小，至多为6
- **`MIN_TREEIFY_CAPACITY = 64`**
  - 转换为树时哈希表的最小容量值
  - 至少为 4 * TREEIFY_THRESHOLD to avoid conflicts between resizing and treeification thresholds.
- **`int modCount`**
  - 结构改变的计数 结构改变：映射的修改 + rehash 的内部结构修改,用来实现遍历时的 fail-fast
- **`int size`**
  - The number of key-value mappings contained in this map
- **`int threshold`**
  - 扩容时的阈值（**capacity * load factor**）
- **`float loadFactor`**
- **`Node<K,V>[] table`**
  - 初次使用时初始化（懒加载），必要时调整大小（始终为2的倍数）
  - 在**首次 put 元素的时候，通过 resize() 方法进行初始化**
- **`Set<Map.Entry<K,V>> entrySet`**
  - Holds cached entrySet(). Note that AbstractMap fields are used for keySet() and values().

### 关键方法

- **`resize() 自动扩容`**

  - 扩容时存在三种情况
    - 哈希桶数组中某个位置只有1个元素，即不存在哈希冲突时，则直接将该元素copy至新哈希桶数组的对应位置即可
    - 哈希桶数组中某个位置的节点为树节点时，则执行红黑树的扩容操作
    - 哈希桶数组中某个位置的节点为普通节点时，则执行链表扩容操作，在JDK1.8中，为了避免之前版本中并发扩容所导致的死链问题，引入了**高低位链表(双端链表)**辅助进行扩容操作

  ```java
  final Node<K,V>[] resize() {
      // 扩容前表
      Node<K,V>[] oldTab = table;
      // 扩容前表容量
      int oldCap = (oldTab == null) ? 0 : oldTab.length;
      // 扩容前表阈值
      int oldThr = threshold;
      int newCap, newThr = 0;
      if (oldCap > 0) {
          if (oldCap >= MAXIMUM_CAPACITY) {
              // 老数组容量大于最大容量
              // 扩容阈值设置为 Integer.MAX_VALUE
              // 如果容量大于等于最大容量则不再扩容
              threshold = Integer.MAX_VALUE;
              return oldTab;
          }
          else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                   oldCap >= DEFAULT_INITIAL_CAPACITY)
              // 旧容量 * 2 < 最大容量而且 旧容量 >= 默认初始容量
              // 新阈值翻倍
              newThr = oldThr << 1; // double threshold
      }
      else if (oldThr > 0) // initial capacity was placed in threshold
          newCap = oldThr;
      else {               // zero initial threshold signifies using defaults
          // 初始化时啥参数也没传
          newCap = DEFAULT_INITIAL_CAPACITY;
          newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
      }
      if (newThr == 0) {
          float ft = (float)newCap * loadFactor;
          newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
      }
      threshold = newThr;
      @SuppressWarnings({"rawtypes","unchecked"})
          Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
      table = newTab;
      if (oldTab != null) {
          for (int j = 0; j < oldCap; ++j) {
              Node<K,V> e;
              if ((e = oldTab[j]) != null) {
                  oldTab[j] = null;
                  if (e.next == null)
                      // 1.不存在 hash 冲突，直接赋值到新 table
                      newTab[e.hash & (newCap - 1)] = e;
                  else if (e instanceof TreeNode)
                      // 2.执行红黑树的扩容
                      ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                  else { // preserve order
                      // 3.链表数据迁移
                      // 【死链解决】采用高低位链表可以解决JDK8之前，链表扩容导致的死链问题。
                      // 先遍历完链表，再插入到相应位置。而之前是每遍历一个就放到哈希桶中。
                      // 低位链表
                      Node<K,V> loHead = null, loTail = null;
                      // 高位链表
                      Node<K,V> hiHead = null, hiTail = null;
                      // 下一节点引用
                      Node<K,V> next;
                      do {
                          // 遍历并创建高低位链表
                          next = e.next;
                          // 为 0 说明扩容后再求余结果不变，为 1 扩容后再求余就是原索引 + 就是数据长度(j + oldCap)
                          // 0: 原位置，对应的是低位链表
                          // 1: 原位置索引+原数组长度，对应的是高位链表
                          if ((e.hash & oldCap) == 0) {
                              if (loTail == null)
                                  loHead = e;
                              else
                                  loTail.next = e;
                              loTail = e;
                          }
                          else {
                              if (hiTail == null)
                                  hiHead = e;
                              else
                                  hiTail.next = e;
                              hiTail = e;
                          }
                      } while ((e = next) != null);
                      // 遍历完再迁移
                      if (loTail != null) {
                          loTail.next = null;
                          newTab[j] = loHead;
                      }
                      if (hiTail != null) {
                          hiTail.next = null;
                          newTab[j + oldCap] = hiHead;
                      }
                  }
              }
          }
      }
      return newTab;
  }
  ```

  

- **`putVal() 添加元素`**

  ```java
  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                 boolean evict) {
      Node<K,V>[] tab; Node<K,V> p; int n, i;
      // 如果 table 为空，调用 resize()方法创建一个  --- 延迟初始化
      if ((tab = table) == null || (n = tab.length) == 0)
          n = (tab = resize()).length;
      if ((p = tab[i = (n - 1) & hash]) == null)
          // (n - 1) & hash  查看对应数组下标是否有元素 p，
          // 没有就 put 进去
          tab[i] = newNode(hash, key, value, null);
      else {
          // 对应数组下标处已经有元素 p 了
          Node<K,V> e; K k;
          if (p.hash == hash &&
              ((k = p.key) == key || (key != null && key.equals(k))))
              // hash 相等，key 值也相等
              e = p;
          else if (p instanceof TreeNode)
              // 如果已存在的 p 是红黑树的子节点，直接加到树里去
              e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
          else {
              for (int binCount = 0; ; ++binCount) {
                  // 链表的情况
                  if ((e = p.next) == null) {
                      // 在后面链接上新加入的节点
                      p.next = newNode(hash, key, value, null);
                      // 如果 binCount 大于树化阈值 - 1
                      if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                          // 树化
                          treeifyBin(tab, hash);
                      break;
                  }
                  // 需要插入的结点已经存在了
                  if (e.hash == hash &&
                      ((k = e.key) == key || (key != null && key.equals(k))))
                      break;
                  p = e;
              }
          }
          if (e != null) {
              // existing mapping for key 已存在 key 的映射关系
              V oldValue = e.value;
              if (!onlyIfAbsent || oldValue == null)
                  // 旧有元素为 null 或指定替换原来的 value
                  e.value = value;
              afterNodeAccess(e); // 它啥也没干
              return oldValue;
          }
      }
      ++modCount;
      if (++size > threshold)
          // 扩容
          resize();
      afterNodeInsertion(evict); // 它也是啥都没干
      return null;
  }
  ```

  

- **`hash(key) 计算 hash 值`**

  - **`(h = key.hashCode()) ^ (h >>> 16)`** 
  - 将 hashCode 的高 16 位与低 16 位进行异或操作--> `扰动函数`
  - **目的**:将高位特定融入低位之中，降低哈希冲突的概率

  ```java
  static final int hash(Object key) {
      int h;
      return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }
  ```

  

- **`treeifyBin() 树化节点`**

  ```java
  // 通过给定的 hash 值替换所有的链表节点。如果表太小了（默认值64），会先扩容表。
  final void treeifyBin(Node<K,V>[] tab, int hash) {
      int n, index; Node<K,V> e;
      // 空表或表大小 < 64，先扩容
      if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
          resize();
      else if ((e = tab[index = (n - 1) & hash]) != null) {
          TreeNode<K,V> hd = null, tl = null;
          do {
              // 把链表节点全部替换成树节点
              TreeNode<K,V> p = replacementTreeNode(e, null);
              if (tl == null)
                  hd = p;
              else {
                  p.prev = tl;
                  tl.next = p;
              }
              tl = p;
          } while ((e = e.next) != null);
          if ((tab[index] = hd) != null)
              hd.treeify(tab);
      }
  }
  ```

  

- **`remove() 删除元素`**

### 考点

#### 1. 哈希冲突时的拉链表法和红黑树(与 AVL 树的对比)

#### 2.目的：使用场景举例，和其他类型 Map 对比，学以致用举例



> **参考：**

[An introduction to optimising a hashing strategy](https://www.todaysoftmag.ro/article/1677/an-introduction-to-optimising-a-hashing-strategy#:~:text=An%20introduction%20to%20optimising%20a%20hashing%20strategy.%20The,especially%20if%20you%20have%20a%20good%20idea%20)

[Java Map中那些巧妙的设计](https://mp.weixin.qq.com/s/7UTEHA6pdHeitg1htzdcRw)

