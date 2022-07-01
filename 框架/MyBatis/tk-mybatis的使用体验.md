### 1. Example

```
好的方面：
	- 不用写sql，简单sql很好实现，面向对象
不好的方面：
  	- 单测代码不好写,Example 会用到 Spring Context 的信息
  	ps：需要在单测方法执行前执行 EntityHelper.initEntityNameMap(Member.class, config);
  	- 稍微复杂点的 sql 如果用 Exeample 的方式会造成代码行数暴增
```

