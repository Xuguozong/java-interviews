### Linux IO 调度器是干什么的

> I/O schedulers attempt to improve throughput by reordering request access into a linear order based on the logical addresses of the data and trying to group these together. While this may increase overall throughput it may lead to some I/O requests waiting for too long, causing latency issues. I/O schedulers attempt to balance the need for high throughput while trying to fairly share I/O requests amongst processes.

将 **IO 请求按地址进行重排序(利用顺序 IO 的特性提升吞吐量)**，能提升总体的吞吐量，但是也会造成某些 IO 请求延时更长。调度器的目标就是**追求高吞吐和 IO 请求的调度公平性。**



### 查看及设置调度策略的命令

> 查看：

```shell
# cat /sys/block/sda/queue/scheduler
[noop] deadline cfq
```



> 设置：

```shell
echo deadline > /sys/block/sda/queue/scheduler
# or
echo deadline | sudo tee /sys/block/sda/queue/scheduler
```



### 调度器种类(以下针对 5.3 以下的内核)

- **`deadline`**

  - 解决了 **IO 饥饿的问题**，为 IO 请求创建了 3 个队列
    - Sorted
    - Read FIFO 按时间顺序读
    - Write FIFO 按时间顺序写
  - **读请求更容易被调度器调度**
  - **读请求 500ms 超时时间，写请求 5s 超时时间**

- **`cfq(Completely Fair Queueing)`**

  - 每个进程为同步 IO 请求创建排序队列
  - 异步 IO 请求只要更少的队列
  - Priorities from ionice are taken into account
  - **追求绝对公平，没有考虑读写操作的不同耗时**

  > Each queue is allocated a time slice for fair queuing. There may be wasteful idle time if a time slice quantum has not expired

- **`noop`**

  > Performs merging of I/O requests but no sorting. Good for random access devices (flash, ramdisk, etc) and for devices that sort I/O requests such as advanced storage controllers.

  - **单个队列，将 IO 请求合并但不重新排序**
  - 适用**随机存储设备如 flash、ram 或者由硬件自己来排序 IO 请求的高级存储控制器**



### IO 调度器参数调优

- [deadline](https://www.kernel.org/doc/Documentation/block/deadline-iosched.txt)
- [cfq](https://www.kernel.org/doc/Documentation/block/cfq-iosched.txt)



### 调优建议

- HDD 不适用 none/noop 调度器因为它们更适合随机 IO，会有寻址延迟的问题
- deadline 和 none/noop 的区别不是很大，但它们和 cfq 差异较大
- MySQL 这类**数据库存储系统不建议用 cfq**
- 对于**虚拟机上的磁盘，建议采样简单的 none/noop ，毕竟数据落盘取决于虚拟化层面**



```
Choosing a Disk Queue Scheduler
On GNU/Linux, the queue scheduler determines the order in which requests to a block device are actually sent to the underlying device.

The default is Completely Fair Queueing, or cfq. It’s okay for casual use on laptops and desktops, where it helps prevent I/O starvation, but it’s terrible for servers. It causes very poor response times under the types of workload that MySQL generates, because it stalls some requests in the queue
needlessly.

You can see which schedulers are available, and which one is active, with the following command:

$ cat /sys/block/sda/queue/scheduler
noop deadline [cfq]

You should replace sda with the device name of the disk you’re interested in. In our example, the square brackets indicate which scheduler is in use for this device.

The other two choices are suitable for server-class hardware, and in most cases they work about equally well. The noop scheduler is appropriate for devices that do their own scheduling behind the scenes, such as hardware RAID controllers and SANs, and deadline is fine both for RAID controllers and disks that are directly attached. Our benchmarks show very little difference between these two. The main thing is to use anything but cfq, which can cause severe performance problems.

Take this advice with a grain of salt, though, because the disk schedulers actually come in many variations in different kernels, and there is no indication of that in their names.
```



> **`参考：`**

[Linux性能调优-磁盘I/O队列调度策略](https://www.cnblogs.com/bamanzi/p/linux-disk-io-scheduler.html)

- [linux-schedulers-in-tpcc-like-benchmark](http://www.mysqlperformanceblog.com/2009/01/30/linux-schedulers-in-tpcc-like-benchmark/)
- [postgresql-linux-kernel-io-tuning](http://www.cybertec.at/postgresql-linux-kernel-io-tuning/)
- [linux-io-scheduler-tuning](https://blog.codeship.com/linux-io-scheduler-tuning/)

[Ubantu-IOSchedulers](https://wiki.ubuntu.com/Kernel/Reference/IOSchedulers)

[Linux Scheduler](https://www.kernel.org/doc/html/latest/scheduler/index.html)

