# 常见问题

## MAC 系统前端无法编译
对于使用了 arm 架构的 mac 电脑，由于 node 安装包没有适配 arm 架构的 release 包，需要通过环境变量的方式让maven认为在 x64 系统下运行：
```bash
mvn clean package -Dos.arch=x64
```