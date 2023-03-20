# 常见问题

## MAC 系统前端无法编译
对于使用了 Arm 架构(即使用了 M1、M2 处理器)的 Mac 电脑，由于 node 安装包没有适配 Arm 架构的 release 包，需要通过环境变量的方式让 Maven 认为在 x64 系统下运行：
```bash
mvn clean package -Dos.arch=x64
```