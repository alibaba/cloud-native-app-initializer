# 代码贡献
我们致力于在云原生时代，构建一个开源、中立、功能强大、社区生态繁荣的脚手架工具。社区欢迎外部用户参与到社区建设中来！

## 如何贡献
在贡献代码之前，请您稍微花一些时间了解为 cloud-native-app-initializer 贡献代码的流程。

### 贡献什么？
我们随时都欢迎任何贡献，无论是简单的错别字修正，BUG 修复还是增加新功能。请踊跃提出问题或发起 PR。我们同样重视文档以及与其它开源项目的整合，欢迎在这方面做出贡献。

如果是一个比较复杂的修改，建议先在 Issue 中添加一个 Feature 标识，并简单描述一下设计和修改点。

### Fork 仓库
* 点击 本项目 右上角的 Fork 图标 将 alibaba/cloud-native-app-initializer fork 到自己的空间。
* 将自己账号下的 cloud-native-app-initializer 仓库 clone 到本地，例如我的账号的 steverao，那就是执行 git clone https://github.com/steverao/cloud-native-app-initializer.git 进行 clone 操作。

### 配置 Github 信息
* 在自己的机器执行 git config  --list ，查看 git 的全局用户名和邮箱。
* 检查显示的 user.name 和 user.email 是不是与自己 github 的用户名和邮箱相匹配。
* 如果公司内部有自己的 gitlab 或者使用了其他商业化的 gitlab，则可能会出现不匹配的情况。这时候，你需要为 cloud-native-app-initializer 项目单独设置用户名和邮箱。
* 设置用户名和邮箱的方式请参考 github 官方文档，设置用户名，设置邮箱。

### Merge 最新代码
fork 出来的代码后，原仓库 Master 分支可能出现了新的提交，这时候为了避免提交的 PR 和 Main 中的提交出现冲突，需要及时 merge Main 分支。

### 配置代码格式规范
作为一个 Spring 相关项目，在后端代码规范方面直接沿用了 Spring Cloud 项目规范，在正式开始之前请参考相关 [代码格式规范说明](https://github.com/spring-cloud/spring-cloud-build#checkstyle) ，提交代码前需要先配置好代码格式规范。

### 开发、提交、Push
开发自己的功能，开发完毕后建议使用 mvn clean install 命令确保能修改后的代码能在本地编译通过。执行该命令的同时还能以 spring 的方式自动格式化代码。然后再提交代码

### merge 最新代码
同样，提交 PR 前，需要 rebase master 分支的代码，具体操作步骤请参考之前的章节。
如果出现冲突，需要先解决冲突。
### 提交Pull Request(PR)
提交 PR，根据 Pull request template 写明修改点和实现的功能，等待 code review 和 合并，成为 Cloud Native App Initializer Contributor，为更好用的脚手架一起努力！

