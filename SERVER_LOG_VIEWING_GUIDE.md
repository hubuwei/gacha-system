# 服务器日志查看指南

## 📋 前提条件

你需要以下信息才能连接到服务器：
- **服务器 IP**: 111.228.12.167
- **用户名**: root（或其他你有权限的用户）
- **密码**: 你的服务器密码（或使用 SSH 密钥）

---

## 方法一：使用 PowerShell SSH 连接（推荐）

### 步骤1：打开 PowerShell

按 `Win + R`，输入 `powershell`，回车

### 步骤2：连接到服务器

```powershell
ssh root@111.228.12.167
```

**首次连接时会提示**：
```
The authenticity of host '111.228.12.167 (111.228.12.167)' can't be established.
Are you sure you want to continue connecting (yes/no/[fingerprint])?
```

输入 `yes` 并回车。

然后会提示输入密码：
```
root@111.228.12.167's password:
```

输入你的服务器密码（输入时不会显示字符），然后回车。

### 步骤3：进入项目目录

连接成功后，你会看到类似这样的提示符：
```
[root@localhost ~]#
```

输入以下命令进入项目目录：
```bash
cd /opt/gacha-system
```

### 步骤4：查看 Docker 容器状态

```bash
# 查看所有运行中的容器
docker-compose ps

# 或者
docker ps
```

**预期输出**：
```
       Name                     Command               State           Ports
-----------------------------------------------------------------------------------
gacha-system-auth-1      java -jar auth-service ...   Up      0.0.0.0:8082->8082/tcp
gacha-system-cms-1       java -jar cms-service.jar    Up      0.0.0.0:8085->8085/tcp
gacha-system-mall-1      java -jar mall-service.jar   Up      0.0.0.0:8081->8081/tcp
...
```

### 步骤5：查看 mall-service 日志

#### 方式A：实时查看日志（推荐）

```bash
# 实时查看 mall-service 的日志（类似 tail -f）
docker-compose logs -f mall-service
```

这会持续显示最新的日志输出，按 `Ctrl + C` 停止。

#### 方式B：查看最近100行日志

```bash
# 查看最近100行日志
docker-compose logs --tail=100 mall-service
```

#### 方式C：查看特定时间段的日志

```bash
# 查看最近1小时的日志
docker-compose logs --since 1h mall-service

# 查看最近30分钟的日志
docker-compose logs --since 30m mall-service
```

#### 方式D：只查看错误日志

```bash
# 查看错误日志
docker-compose logs mall-service | grep ERROR

# 或者查看包含 Exception 的行
docker-compose logs mall-service | grep Exception
```

### 步骤6：查看 cms-service 日志

```bash
# 实时查看 cms-service 日志
docker-compose logs -f cms-service

# 查看最近50行
docker-compose logs --tail=50 cms-service
```

### 步骤7：查看所有服务的日志

```bash
# 查看所有服务的实时日志
docker-compose logs -f

# 查看最近20行所有服务日志
docker-compose logs --tail=20
```

### 步骤8：进入容器内部查看日志文件

如果 Actuator 配置的日志文件在容器内，可以进入容器查看：

```bash
# 进入 mall-service 容器
docker-compose exec mall-service bash

# 然后在容器内查看日志文件
cd logs
ls -lh

# 查看最新的 info 日志
tail -f mall-service_info.log

# 查看 error 日志
cat mall-service_error.log

# 退出容器
exit
```

### 步骤9：从容器复制日志文件到服务器

```bash
# 从 mall-service 容器复制日志文件到服务器当前目录
docker cp gacha-system-mall-1:/app/logs/mall-service_info.log ./mall-service_info.log

# 查看文件大小
ls -lh mall-service_info.log

# 查看最后50行
tail -n 50 mall-service_info.log
```

---

## 方法二：使用 PuTTY（Windows 图形界面工具）

如果你不熟悉命令行，可以使用 PuTTY 图形化工具。

### 步骤1：下载 PuTTY

访问：https://www.putty.org/
下载并安装 PuTTY

### 步骤2：配置连接

1. 打开 PuTTY
2. 在 "Host Name (or IP address)" 输入：`111.228.12.167`
3. Port 保持默认：`22`
4. Connection type 选择：`SSH`
5. 点击 "Open"

### 步骤3：登录

1. 首次连接会弹出安全警告，点击 "Yes"
2. 输入用户名：`root`
3. 输入密码（不会显示）
4. 登录成功

### 步骤4：执行上述命令

登录后，按照**方法一**的步骤3-9执行命令即可。

---

## 方法三：使用 VS Code Remote SSH（最方便 ⭐⭐⭐）

如果你有 VS Code，这是最方便的方式。

### 步骤1：安装 Remote SSH 扩展

1. 打开 VS Code
2. 点击左侧扩展图标（或按 `Ctrl + Shift + X`）
3. 搜索 "Remote SSH"
4. 安装 "Remote - SSH" 扩展（Microsoft 出品）

### 步骤2：配置 SSH 连接

1. 按 `F1` 或 `Ctrl + Shift + P`
2. 输入 "Remote-SSH: Connect to Host"
3. 选择 "Add New SSH Host"
4. 输入：`ssh root@111.228.12.167`
5. 选择配置文件（默认第一个）
6. 点击 "Connect"

### 步骤3：输入密码

会提示输入密码，输入后保存（可选）。

### 步骤4：打开远程终端

连接成功后：
1. 点击菜单 "Terminal" → "New Terminal"
2. 现在你可以在 VS Code 中直接使用终端了

### 步骤5：打开远程文件夹

1. 按 `F1`
2. 输入 "File: Open Folder"
3. 输入：`/opt/gacha-system`
4. 现在你可以直接在 VS Code 中浏览和编辑服务器上的文件了！

### 步骤6：查看日志

在 VS Code 终端中执行上述命令，或者直接：
1. 在左侧文件浏览器中找到日志文件
2. 双击打开即可查看
3. 支持实时刷新

---

## 🔍 常用日志排查命令

### 1. 查找最近的错误

```bash
# 查找最近10个错误
docker-compose logs mall-service | grep ERROR | tail -10

# 查找包含 NullPointerException 的错误
docker-compose logs mall-service | grep NullPointerException
```

### 2. 统计错误数量

```bash
# 统计今天的错误数量
docker-compose logs --since 24h mall-service | grep -c ERROR

# 统计警告数量
docker-compose logs --since 24h mall-service | grep -c WARN
```

### 3. 查看特定类的日志

```bash
# 查看 ProductService 相关的日志
docker-compose logs mall-service | grep ProductService

# 查看 OrderController 相关的日志
docker-compose logs mall-service | grep OrderController
```

### 4. 查看慢请求

```bash
# 查找耗时超过1秒的请求（假设有相关日志）
docker-compose logs mall-service | grep "timeTaken" | awk '{if ($NF > 1000) print}'
```

### 5. 监控资源使用

```bash
# 查看容器的资源使用情况
docker stats

# 查看 mall-service 容器的详细资源使用
docker stats gacha-system-mall-1
```

### 6. 重启服务

```bash
# 重启 mall-service
docker-compose restart mall-service

# 重启所有服务
docker-compose restart

# 停止并重新启动
docker-compose down
docker-compose up -d
```

### 7. 检查服务健康状态

```bash
# 在服务器上执行
curl http://localhost:8081/api/actuator/health

# 或者从容器外部访问
curl http://111.228.12.167:8081/api/actuator/health
```

---

## 📊 Actuator 监控端点远程访问

由于 Actuator 端点在服务器上运行，你可以通过以下方式访问：

### 方式1：在服务器本地访问

```bash
# 在 SSH 连接中执行
curl http://localhost:8081/api/actuator/health | python3 -m json.tool

# 查看指标
curl http://localhost:8081/api/actuator/metrics/jvm.memory.used
```

### 方式2：从本地浏览器访问（需要开放端口）

如果服务器的 8081 端口已对外开放：

```
http://111.228.12.167:8081/api/actuator/health
http://111.228.12.167:8081/api/actuator/metrics
http://111.228.12.167:8081/api/actuator/httptrace
```

**注意**：出于安全考虑，生产环境可能限制了 Actuator 端点的远程访问。

### 方式3：使用 SSH 隧道（推荐 ⭐）

在你的本地 PowerShell 中执行：

```powershell
# 建立 SSH 隧道，将本地的 9081 端口映射到服务器的 8081 端口
ssh -L 9081:localhost:8081 root@111.228.12.167
```

然后在本地浏览器访问：
```
http://localhost:9081/api/actuator/health
http://localhost:9081/api/actuator/metrics
```

这样既安全又方便！

---

## 🐛 常见问题排查

### 问题1：无法连接到服务器

**可能原因**：
- 网络不通
- SSH 服务未启动
- 防火墙阻止

**解决方法**：
```powershell
# 测试网络连通性
ping 111.228.12.167

# 测试 SSH 端口是否开放
Test-NetConnection 111.228.12.167 -Port 22
```

### 问题2：密码错误

**解决方法**：
- 确认密码是否正确（注意大小写）
- 联系服务器管理员重置密码
- 如果使用密钥登录，检查密钥路径是否正确

### 问题3：docker-compose 命令找不到

**解决方法**：
```bash
# 检查 Docker 是否安装
docker --version

# 检查 docker-compose 是否安装
docker-compose --version

# 如果未安装，参考 deploy-to-server.sh 脚本安装
```

### 问题4：容器未运行

**解决方法**：
```bash
# 查看所有容器（包括停止的）
docker-compose ps -a

# 启动所有服务
docker-compose up -d

# 查看启动日志
docker-compose logs
```

### 问题5：日志太多，看不清

**解决方法**：
```bash
# 只看最近50行
docker-compose logs --tail=50 mall-service

# 过滤只显示错误
docker-compose logs mall-service | grep ERROR

# 导出到文件慢慢看
docker-compose logs mall-service > mall-logs.txt
```

---

## 📝 快速操作清单

连接到服务器后，按顺序执行：

```bash
# 1. 进入项目目录
cd /opt/gacha-system

# 2. 查看容器状态
docker-compose ps

# 3. 查看 mall-service 最近50行日志
docker-compose logs --tail=50 mall-service

# 4. 查看是否有错误
docker-compose logs mall-service | grep ERROR | tail -20

# 5. 检查健康状态
curl http://localhost:8081/api/actuator/health

# 6. 实时监控日志（按需）
docker-compose logs -f mall-service
```

---

## 💡 小贴士

### tip1：保存 SSH 配置

在本地创建 `~/.ssh/config` 文件（PowerShell 中是 `$HOME\.ssh\config`）：

```
Host gacha-server
    HostName 111.228.12.167
    User root
    Port 22
```

然后就可以简化连接命令：
```powershell
ssh gacha-server
```

### tip2：使用 tmux 或 screen

如果日志输出很多，可以使用 tmux 防止断开连接后丢失：

```bash
# 安装 tmux
yum install tmux  # CentOS
apt install tmux  # Ubuntu

# 创建新会话
tmux new -s logs

# 在里面执行日志查看命令
docker-compose logs -f mall-service

# 按 Ctrl+B，然后按 D 退出会话（后台运行）

# 重新连接会话
tmux attach -t logs
```

### tip3：设置日志别名

在 `~/.bashrc` 中添加：

```bash
alias mall-logs='docker-compose logs --tail=50 mall-service'
alias mall-logs-follow='docker-compose logs -f mall-service'
alias mall-errors='docker-compose logs mall-service | grep ERROR | tail -20'
alias cms-logs='docker-compose logs --tail=50 cms-service'
```

然后执行 `source ~/.bashrc`，之后就可以直接输入 `mall-logs` 查看日志了。

---

## 🎯 下一步

1. ✅ 代码已提交到 GitHub 和码云
2. ⏳ 使用上述方法连接服务器
3. ⏳ 查看 mall-service 和 cms-service 的日志
4. ⏳ 验证 Actuator 监控端点是否正常工作
5. ⏳ 检查日志文件格式是否符合预期

---

**祝你顺利连接到服务器！** 🚀

如有任何问题，随时告诉我！
