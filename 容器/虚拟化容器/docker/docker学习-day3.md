# docker命令积累

### 1.查看容器日志

> `docker logs [容器ID]`

    ## 实时监控容器日志
    docker logs -f [容器ID]


### 2.查看容器详情

> `docker inspect [容器ID]`


### 3.查看容器内进程

> `docker top [容器ID]`

### 4.导入导出容器

> 导出： docker export [容器ID] > xxx.tar
> 导入： docker import xxx.tar [容器名]