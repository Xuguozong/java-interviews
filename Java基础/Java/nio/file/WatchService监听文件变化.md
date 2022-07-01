### 1. 注册监视服务

```java
// 获取监视服务
WatchService watcher = FileSystems.getDefault().newWatchService();
// 监视的文件或文件夹
Path toWatch = Paths.get(TEST_DIR);
// 绑定要监视的事件类型并注册到监视服务中
toWatch.register(watcher,
	// 注册监听类型
	new WatchEvent.Kind[]{ StandardWatchEventKinds.ENTRY_CREATE },
	// 指定监听敏感度(几秒内做出响应)
	SensitivityWatchEventModifier.HIGH);
```

### 2. 单开线程做相应处理

```java
@Override
public void run() {
    System.out.println("Begin Watching");
    while (true) {
        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        // 拉取所有发生的事件
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                Path created = (Path) event.context();
                System.out.println(created);
                try {
                    this.loadClass(created.getFileName().toString(), true);
                } catch (ClassNotFoundException e) {
                    System.out.println("Unable to find class");
                }
            }
        }
        key.reset();
    }
}
```

