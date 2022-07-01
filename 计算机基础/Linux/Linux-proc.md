## [proc](https://man7.org/linux/man-pages/man5/proc.5.html)
### 总述
> The proc filesystem is a pseudo-filesystem which provides an
interface to kernel data structures.
> Most of the files in the proc filesystem are read-only, but some
files are writable, allowing kernel variables to be changed.

```
proc 是一个提供 OS 内核数据结构接口的伪文件系统，大多数文件都是只读文件，部分可写文件能够控制进程和内核的行为。
提供内核统计信息的文件系统接口，由内核动态创建，在内存中运行
```

### 结构总览

- `/proc/[pid] `

  ​	**进程相关信息**

  - `task/[tid]`

    *进程内线程相关信息，tid 是 kernel thread ID*

- `/proc/[tid] `

  **[线程信息,可能不可见](https://man7.org/linux/man-pages/man2/getdents.2.html)**

- `/proc/self `

  **符号链接（symbolic link），接入这个的进程会进入到自己的 `/proc/[pid] `目录中去**

- `/proc/ thread-self`

  **符号链接（symbolic link），接入这个的进程会进入到自己的 `/proc/self/task/[tid] `目录中去**

- `/proc/ [a-z]*`

  **表示系统层面信息的文件或子目录**

### 具体结构

- 进程级别
  - **limits**：实际的资源限制
  - **maps**：映射的内存区域
  - **sched**：CPU 调度器的各种统计
  - **schedstat**：CPU 运行时间、延时和时间分片
  - **smaps**：映射内存区域的使用统计
  - **stat**：进程状态和统计，包括总的 CPU 和内存的使用情况
  - **statm**：以页为单位的内存使用总结
  - **status**：stat 和 statm 的信息，用户可读
  - **task**：每个任务的统计目录
- 系统级别
  - **cpuinfo**：物理处理器信息，包含所有虚拟 CPU、型号、时钟频率和缓存大小
  - **diskstats**：对于所有磁盘设备的磁盘 I/O 统计
  - **interrupts**：每个 CPU 的中断计数器
  - **loadavg**：负载平均值
  - **meminfo**：系统内存使用明细
  - **net/dev**：网络接口统计
  - **net/tcp**：活跃的 TCP 套接字信息
  - **schedstat**：系统级别的 CPU 调度器统计
  - **self**：关联当前进程 ID 的符号链接，为了使用方便
  - **slabinfo**：内存 slab 分配器缓存统计
  - **stat**：内核和系统资源的统计，CPU、磁盘、分页、交换区、进程
  - **zoneinfo**：内存区信息

### 用途

- top 命令的实现
  - 读取 /proc 文件夹下各个进程的运行时数据和系统总的运行数据



### net ipv4 内核相关参数在 /proc/sys/net/ipv4 下面

- tcp_rmem
- tcp_wmem
- tcp_mem
- tcp_keepalive_time  默认 7200s
- tcp_keepalive_probes 发送探测包的次数,默认 9 次
- tcp_keepalive_intvl  发送探测包的间隔 默认 75s
- 等等