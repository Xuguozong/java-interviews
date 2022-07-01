# docker命令积累-网络

### 1. 查看网络

	docker network ls

### 2. 新建网络

	docker network create -d bridge [name]

### 3. 容器运行时指定网络

	docker run --name [app name] --network [network name] ...
    例如： app 需要连接 mysql，直接将 mysql 和 app 运行在同一个网络下