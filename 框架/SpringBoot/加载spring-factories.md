### 入口

> **SpringApplication#getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args)**

```
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
	ClassLoader classLoader = getClassLoader();
	// Use names and ensure unique to protect against duplicates
	// 读取各个 spring jar 下的 META-INF/spring.factories 文件并缓存
	Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
	// 利用反射创建实例对象
	List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
	AnnotationAwareOrderComparator.sort(instances);
	return instances;
}
```

