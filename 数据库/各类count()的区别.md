- **`count(field)`**
  - 如果字段 not null,一行行地**从记录里面读出这个字段**，判断不能
    为 null，按行累加
  - 如果字段允许 null ,执行的时候，判断到有可能是 null，还要把值取出
    来再判断一下，不是 null 才累加
- **`count(primary key)`**
  - **InnoDB 引擎会遍历整张表，把每一行的 id 值都取出来，返回给
    server 层**。server 层拿到 id 后，判断是不可能为空的，就按行累加
- **`count(1)`**
  - **InnoDB 引擎遍历整张表，但不取值。server 层对于返回的每一行，放一
    个数字“1”进去，判断是不可能为空的，按行累加**
- **`count(*)`**
  - 例外，并不会把全部字段取出来，而是专门做了优化，不取值。count(*) 肯定
    不是 null，按行累加

#### 效率排序

> **count(字段)<count(主键 id)<count(1)≈count(*)**

