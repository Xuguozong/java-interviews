### 1. 前期服务器准备

```shell
yum install -y curl policycotrutils-python openssh-server perl vim wget
systemctl enable sshd
systemctl start sshd
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
systemctl reload firewalld
# 关闭防火墙
systemctl stop firewalld
systemctl disable firewalld
# 关闭 selinux
vim /etc/selinux/conf  -->  SELINUX=disabled  --> 然后重启生效
# 下面为可选：安装邮件服务
sudo yum install postfix
sudo systemctl enable postfix
sudo systemctl start postfix
```



### 2. 安装 gitlab & gitlab-runner

#### 2.1 gitlab 安装

[gitlab社区版下载页面](https://about.gitlab.cn/install/?test=capabilities)

```shell
# 下载安装包
wget https://omnibus.gitlab.cn/el/7/gitlab-jh-14.9.0-jh.0.el7.x86_64.rpm
# 安装
sudo rpm -Uvh gitlab-jh-14.9.0-jh.0.el7.x86_64.rpm
```

- 配置 gitlab

```shell
# 编辑 /etc/gitlab/gitlab.rb 文件，并设置:
# root⽤户初始化密码
gitlab_rails['initial_root_password'] = '<my_strong_password>'
# 浏览器访问的ip+端⼝，ip就是安装gitlab机器的ip
external_url 'http://gitlab.example.com'
# 设置端⼝
nginx['listen_port'] = nil

## 执行 gitlab 配置命令：
gitlab-ctl reconfigure
## 重启 gitlab
gitlab-ctl restart
```

#### 2.2 gitlab-runner 安装

[官方参考文档](https://docs.gitlab.com/runner/install/linux-manually.html#install-gitlab-runner-manually-on-gnulinux)

```shell
## 下载 gitlab-runner 二进制执行程序
sudo curl -L --output /usr/local/bin/gitlab-runner https://gitlab-runner-downloads.s3.amazonaws.com/latest/binaries/gitlab-runner-linux-amd64

## 修改执行权限
sudo chmod a+x /usr/local/bin/gitlab-runner

## 创建 gitlab 用户
sudo useradd --comment 'GitLab Runner' --create-home gitlab-runner --shell /bin/bash

## 安装并作为服务运行(以 root 用户执行，否则可能在打包部署阶段因为权限问题发生错误)
sudo gitlab-runner install --user=root --working-directory=/home/gitlab-runner
sudo gitlab-runner start

## 注册 runner
gitlab-runner register
```



### 3. 安装 docker

- 更新 docker-ce 的 yum 源

  ```shell
  wget -O /etc/yum.repos.d/docker-ce.repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
  ```

- 执行安装

  > yum install -y docker-ce

- 配置镜像加速器

  ```shell
  # 编辑 docker daemon 配置文件，/etc/docker/daemon.json
  sudo mkdir -p /etc/docker
  sudo tee /etc/docker/daemon.json <<-'EOF'
  {
    "registry-mirrors": ["https://uzw5ttt8.mirror.aliyuncs.com"]
  }
  EOF
  sudo systemctl daemon-reload
  sudo systemctl restart docker
  ```

[参考阿里云](https://help.aliyun.com/document_detail/51853.html#section-gtl-cjs-ls2)



### 4. 安装 JDK & Maven

#### 4.1 安装 jdk

```shell
## JRE
yum install -y java-1.8.0-openjdk.x86_64
## JDK
yum install -y java-1.8.0-openjdk-devel.x86_64
```

#### 4.2 安装 maven

[下载地址](https://archive.apache.org/dist/maven/maven-3/)

```shell
## 下载
wget https://archive.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz

## 解压
tar -zxvf apache-maven-3.5.4-bin.tar.gz

## 配置环境变量(也可以不配置，指定 maven 可执行文件路径进行打包构建工作)
vim /etc/profile
export MAVEN_HOME=/opt/maven-3.5.4
export PATH=$PATH:$MAVEN_HOME/bin

## 激活
source /etc/profile
```



### 5. 编辑 .gitlab-ci.yml

```yaml
stages:
  - build
  - deploy

#image: centos:centos8.4.2105

maven-build:
  stage: build
  script: # 执行脚本
    - mvn clean package -DskipTests && echo 打包完成
  artifacts:
    paths:
      - eomcs/target/eomcs.jar # 成果物
  only:
    - main # 执行分支

#
# deploy
#
deploy:
  stage: deploy
  script: # 部署命令
    - nohup java -jar eomcs/target/eomcs.jar &
  only:
    - main
```



### 6. 以 docker 方式部署

- Dockerfile

  ```dockerfile
  FROM openjdk:8-alpine
  RUN apk add --no-cache tini
  ADD eomcs/target/eomcs.jar eomcs.jar
  ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms512m -Xmx512m -Xmn320m -XX:MaxTenuringThreshold=15 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xloggc:/logs/gc.log -Djava.security.egd=file:/dev/./urandom"
  EXPOSE 7801
  ENTRYPOINT ["/sbin/tini"]
  
  CMD sleep 60; java -jar eomcs.jar $JAVA_OPTS
  ```

  

- .gitlab-ci.yml

  ```yaml
  stages:
    - build
    - docker-build
    - deploy
  
  #image: centos:centos8.4.2105
  
  maven-build:
    stage: build
    script:
      - mvn clean package -DskipTests && echo 打包完成
    artifacts:
      paths:
        - eomcs/target/eomcs.jar
    only:
      - main
  
  docker-image-build:
    stage: docker-build
    script:
      - docker stop eomcs && docker rm eomcs && docker rmi eomcs:3
      - docker build -t eomcs:3 .
  
  #
  # deploy
  #
  deploy:
    stage: deploy
    script:
      # - nohup java -jar eomcs/target/eomcs.jar &
      - docker run -d --name eomcs -p 7801:7801 eomcs:3
    only:
      - main
  ```

  