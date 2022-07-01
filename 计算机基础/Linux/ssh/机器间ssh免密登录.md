1. 生成 rsa 公钥
	ssh-keygen -t rsa
	cd /rrot/.ssh
	cp id_rsa.pub authorized_keys
2. /etc/hosts 配置目标机器 domain
3. ssh-copy-id -i [目标机器]
4. 互相ssh-copy-id 或者 scp authorized_keys username@目标机器:/root/.ssh