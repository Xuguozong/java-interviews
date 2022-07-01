Spring 和 MyBatis 解析 XML 配置文件的异同

1. 大体流程
	- dtd 文件解读（是否需要网络下载）
	- 标签节点属性解读
	- 反射创建类实例
2. MyBatis
	- 如何把连接信息搞进 Connection 对象中的