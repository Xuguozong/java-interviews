1. 备份现有数据

   ```shell
   wsl --export docker-desktop-data "D:\Docker\data\docker-desktop-data.tar"
   wsl --export docker-desktop D:\data\docker-desktop\data.tar
   ```

2. 删除原有数据

   ```shell
   wsl --unregister docker-desktop-data
   wsl --unregister docker-desktop
   wsl --list -v
   ```

3. 导入数据到新盘

   ```shell
   wsl --import docker-desktop-data "D:\Docker\data" "D:\Docker\data\docker-desktop-data.tar" --version 2
   wsl --import docker-desktop D:\data\docker-desktop D:\data\docker-desktop\data.tar
   ```

4. [参考](https://www.jianshu.com/p/e79f4c938000)