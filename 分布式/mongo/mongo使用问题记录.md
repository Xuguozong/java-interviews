### 1. decimal 类型字段

```java
@Field(targetType = FieldType.DECIMAL128)
// 不指定类型的话，默认转为 string 在 Mongo 中存储
private BigDecimal amount;
```



### 2. string 类型数值字段求和

- 需要传入数据转换器定义数据转换逻辑

```java
Aggregation.group("_class")
	// 需要传入数据转换器定义数据转换逻辑
	.sum(ConvertOperators.ToDecimal.toDecimal("$amount"))
	.as("amount");
```



### 3. MongoTemplate 执行 mapReduce 脚本报错

```
阿里云生产环境的 Mongo 禁止了客户端执行 js 脚本
```

