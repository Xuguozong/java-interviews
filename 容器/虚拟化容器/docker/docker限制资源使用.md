> 实现原理：Linux 的 cgroups 控制群组，用来限制、控制和分离一个进程组(如CPU、内存、IO等)

### 1. CPU

> 默认可以无限制使用 cpu 资源

- **-c/--cpu-shares**

  > 在有多个容器竞争 CPU 时我们可以设置每个容器能使用的 CPU 时间比例。这个比例叫作共享权值。共享式CPU资源，是按比例切分CPU资源；Docker 默认每个容器的权值为 1024。如果不指定或将其设置为0，都将使用默认值。 比如，当前系统上一共运行了两个容器，第一个容器上权重是1024，第二个容器权重是512， 第二个容器启动之后没有运行任何进程，自己身上的512都没有用完，而第一台容器的进程有很多，这个时候它完全可以占用容器二的CPU空闲资源，这就是共享式CPU资源；如果容器二也跑了进程，那么就会把自己的512给要回来，按照正常权重1024:512划分，为自己的进程提供CPU资源。
  >
  > 与内存限额不同，通过-c设置的cpu share 并不是CPU资源的绝对数量，而是一个相对的权重值。某个容器最终能分配到的CPU资源取决于它的cpu share占所有容器cpu share总和的比例。**换句话说，通过cpu share可以设置容器使用CPU的优先级。**

- **--cpus**

  > 限制容器运行的核数

- **--cpuset-cpus**

  > 限制容器运行在指定的CPU核心； 运行容器运行在哪个CPU核心上，例如主机有4个CPU核心，CPU核心标识为0-3，我启动一台容器，只想让这台容器运行在标识0和3的两个CPU核心上，可以使用cpuset来指定

#### CPU 资源的绝对限制

Linux 通过 CFS（Completely Fair Scheduler，完全公平调度器）来调度各个进程对 CPU 的使用。CFS 默认的调度周期是 100ms。

我们可以设置每个容器进程的调度周期，以及在这个周期内各个容器最多能使用多少 CPU 时间。

- --cpu-period 设置每个容器进程的调度周期

- --cpu-quota 设置在每个周期内容器能使用的 CPU 时间

  > CFS 周期的有效范围是 `1ms~1s`，对应的--cpu-period的数值范围是 `1000~1000000`。
  >
  > 而容器的 CPU 配额必须不小于 1ms，即--cpu-quota的值必须 >= 1000。可以看出这两个选项的单位都是 us。

例如：

```shell
docker run -it --cpu-period=50000 --cpu-quota=25000 Centos centos /bin/bash
```

表示将 CFS 调度的周期设为 50000，将容器在每个周期内的 CPU 配额设置为 25000，表示该容器每 50ms 可以得到 50% 的 CPU 运行时间。



### 2. 内存

- -m 或 --memory：设置内存的使用限额，例如：100MB，2GB。
- --memory-swap：设置**内存+swap**的使用限额。

例如：

```shell
# 允许该容器最多使用200MB的内存和100MB 的swap。
docker run -m 200M --memory-swap=300M ubuntu


# 容器最多使用200M的内存和200M的Swap
docker run -it -m 200M ubuntu
```



### 3. block 磁盘 IO

Block IO 是另一种可以限制容器使用的资源。Block IO 指的是磁盘的读写，docker 可通过设置权重、限制 bps 和 iops 的方式控制容器读写磁盘的带宽

注：目前 Block IO 限额只对 direct IO（不使用文件缓存）有效。



#### 如何进行Block IO的限制？

默认情况下，所有容器能平等地读写磁盘，可以通过设置 `--blkio-weight` 参数来改变容器 block IO 的优先级。 `--blkio-weight` 与 `--cpu-shares` 类似，设置的是相对权重值，默认为 500。在下面的例子中，container_A 读写磁盘的带宽是 container_B 的两倍。

```shell
docker run -it --name container_A --blkio-weight 600 ubuntu
docker run -it --name container_B --blkio-weight 300 ubuntu
```



#### 如何对bps和iops进行限制？

bps 是 byte per second，表示每秒读写的数据量。

iops 是 io per second，表示每秒的输入输出量(或读写次数)。

可通过以下参数控制容器的 bps 和 iops：

- --device-read-bps，限制读某个设备的 bps。
- --device-write-bps，限制写某个设备的 bps。
- --device-read-iops，限制读某个设备的 iops。
- --device-write-iops，限制写某个设备的 iops。



[docker资源限制](https://zhuanlan.zhihu.com/p/417472115)