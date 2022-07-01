[TOC]

### 1.查看提交日志

> `git log`

     # 一行显示一条提交记录   
     git log --pretty=oneline
     
     # 查看某个文件每次提交记录的区别
     git log -p filename
     
     # 查看某个人最近的三次提交
     git log --oneline -3 --author="name"

### 2.退回之前版本

> `git reset`

```shell
# 退回至上一版本
git reset --hard HEAD^  

## HEAD表示当前版本，HEAD^表示上一版本，HAED~100表示前100个版本

# 退回某一版本
git reset --hard [commit id]

# 强推到远程
git push origin HEAD --force
```

### 3.查询历史命令

> `git reflog`

### 4.撤销修改

```shell
# 只修改了文件，没添加到暂存区(git add)
git checkout -- file

# 修改了文件，添加到了暂存区，还没提交(git commit)
git reset HEAD file

# 修改了文件，添加到了暂存区，而且已提交，还没推送到远程库
git reset 版本退回
```

### 5.删除文件

> `git rm`

    # 正常删除
    git rm file 
    git commit -m "blablabla..."
    
    # 误删
    git checkout -- file



###  6.添加远程骨架仓库并同步

git remote add skeleton https://github.com/Berkeley-CS61B/skeleton-sp21

git pull skeleton master  添加远程骨架仓库并同步

git checkout commitId 进入某个commit版本

git remote -v 查看远程仓库信息

git push origin master 推送远程仓库

git checkout commitId -- 文件/文件夹  将某个文件或文件夹回退到固定commitId的版本，然后 git push 完成最终回退

### 7. git pull origin master 报错

```
fatal: refusing to merge unrelated histories
```

```
解决办法：git pull origin master --allow-unrelated-histories
```

### 8. 合并分支 Commit 到 Master 主干

```
git cherry-pick [commitId] | feature  # feature 表示提交该分支的最后一个 commit
```

[git cherry-pick 教程](https://ruanyifeng.com/blog/2020/04/git-cherry-pick.html)

### 9. git stash 暂存

```
git add 要暂存的内容
git stash
git stash list  查看已经暂存的内容
// 切换其他分支做一些事情
// 切换回暂存了的分支
git stash apply 恢复暂存的内容
```

### 10. Linux 环境下　clone 的文件默认都被　modified

> git config core.fileMode false

