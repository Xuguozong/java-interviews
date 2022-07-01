# docker学习-day2(镜像与容器操作)

### 1.镜像操作

> `搜索镜像：`**docker search [OPTIONS] TERM**

	# options选项
	--filter,-f   过滤规则
	--limit       显示的结果数量（默认25）
	--no-trunc    输出原始结构
	--stars		  指定最小的star数（默认0）
	
	# 实例:
	# 搜索官方发行的star在3以上的centos镜像
	docker search --filter "is-official=true" stars=3 centos


> `拉取镜像：`**docker pull [OPTIONS] NAME[:TAG|@DIGEST]**

	# options 选项
	--all-tags,-a  拉取所有打了tag的镜像
	--disable-content-trust  跳过镜像验证

> `列出镜像：`**docker images [OPTIONS] [Repository[:TAG]]**

	# options 选项
	--all,-a       列出所有镜像（包括中间层镜像）
	--digests      显示摘要
	--filter,-f    过滤规则（"key=value"）
	--format <string> 格式化（使用go语言模板）
	--no-trunc     显示原始结构
	--quiet,-q     只显示数字IDs
	
	# 实例:
	# 列出虚悬镜像
	docker images --filter "dangling=true"
	# 删除所有虚悬镜像
	docker rmi $(docker images -f "dangling=true" -q)
	# --format实例
	docker images --format "table {{.ID}}\t{{.Repository}}\t{{.Tag}}

> `删除镜像：`**docker rmi [OPTIONS] IMAGE [IMAGE...]**

	# options 选项
	--force,-f     强制删除
	--no-prune     不删除untagged的父类镜像
	
	# 实例:
	# 删除所有仓库名为redis的镜像
	docker rmi $(docker images -q redis)
	# 删除所有在mongo:3.2之前的镜像
	docker rmi $(docker images -q -f before=mongo:3.2)

---

### 2.容器操作

> `从镜像启动容器：`**docker run [OPTIONS] IMAGE [COMMAND] [ARG...]**

	# options 选项
	-t     分配一个伪终端，并绑定容器的标准输入
	-i,--interactive   交互模式，让容器的标准输入保持打开
	--name 给容器命名
	-w,--workdir [/path/to]  指定容器终端的工作地址
	-p,--publish     端口映射
	--restart 退出重启[no|failure|always]    
	--rm   容器退出时自动删除容器
	-d,--detach 后台运行并打印容器ID
	--privileged 特权模式，对宿主机有root访问权限，存在一定安全风险
	
	# 实例: 
	docker run -i -t centos bash
	docker run -it centos /bin/bash

> `启动已终止容器：`**docker start [OPTIONS] CONTAINER [CONTAINER...]**

	# options 选项
	-i  交互模式

> `查看容器：`

	# 查看运行容器
	docker ps
	# 查看所有容器
	docker ps -a
	# 查看容器启动命令
	docker ps -a --no-trunc
	# 查看最后x个容器
	docker ps -n x

> `终止容器：`**docker stop CONTAINER**

> `进入容器：`**docker attach CONTAINER**

	# 实例:
	docker run --name test -d -it centos
	docker attach test

> `导出容器：`**docker export [OPTIONS] CONTAINER**

	# options 选项
	--output,-o   导出到文件
	
	# 实例:
	docker export centos > centos.rar
	docker export --output="centos.rar" centos

> `导入容器：`**docker import [OPTIONS] file|URL|- [REPOSITORY[:TAG]]**

	# options 选项
	--change,-c  Apply Dockerfile instruction to created image
	--message,-m  Set commit message for imported message
	
	# 实例:
	docker import - exampleimage.tgz

> `删除容器：`**docker rm [OPTIONS] CONTAINER [CONTAINER...]**

	# options 选项
	--force,-f   强制删除运行时容器（sigkill）
	--link,-l    Remove the specified link
	--volumes,-v 删除与容器相关的数据卷
	
	# 实例:
	# 删除所有的终止容器
	docker rm $(docker ps -a -q)
