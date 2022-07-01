### 1.redis pod实例重启后无法创建集群的问题
> `问题描述：`nodes.info文件持久化到宿主机磁盘后，如果redis实例重启会加载之前的nodes.conf信息作为新的集群节点信息，导致集群不成功

> `方案：`放弃nodes.conf文件的持久化

### 2.使用redis-trib镜像作为集群管理工具遇到的问题

> `问题描述：`redis-trib创建集群是通过redis节点的ip+port实现的，而作为redis节点载体的pod在每次重启时ip都会改变，redis-trib是无法感知
> pod的ip变化的。因此需要借助kubectl命令行工具和脚本实现集群的部署，但这又和hae的功能用途不合。

> `方案：`前期：一个redis节点对应一个pod，这个pod再对应一个service，固定service的cluster ip，这样可以无需通过kubectl创建集群了
>  `存在的问题：`在后续给主节点添加从节点的时候失败：从节点是通过nodes.conf文件中主节点的nodeID信息发现主节点的，但在nodes.conf文件中保存的
>  还是pod的ip而不是service的cluster ip，因此无法完成从节点的添加。
>  `后期：`放弃redis-trib管理集群的方式，自己封装jedis集群相关操作打包成镜像应用作为集群的管理工具。

### 3.redis集群的数据持久化问题
> `问题描述：`当有数据持久化的需求时，如何将redis数据持久化

> `方案1：`通过hostPath的方式将redis节点数据持久化到node节点上
> `存在的问题：`pod重启后可能会被分配在不同node节点上，导致数据分散的问题

> `方案2：`为了解决方案1中的问题，放弃hostPath的挂载方式，采用k8s中网络存储的方案

### 4.node宕机重启后，部分分片区域不可用

> `问题描述：`redis主从节点在同一个node上，node宕机重启后，pod ip改变，这个主从节点负责的分片区域不可用

> `方案：`在集群创建的时候就做好规划，尽量不要让主节点和它的从节点在同一个node上

### 5.Redis Cluster集群不支持一些少数场景下的命令

> `问题描述：`例如redis-cluster的客户端驱动JedisCluster不支持pipeline操作

> `方案：`需要自己开发相关命令操作代码或者注明不支持的操作类型
