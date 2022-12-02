# Maven编译前端流程

> tips: 步骤 1、2 是将前端源码编译成结果文件，如果前端代码没有变更，可以不执行。
> 编译后的结果存放在 public 目录中，这个目录的文件会被提交到仓库里。

1. 安装node和yarn
    
    mvn compile -P install-yarn

2. 编辑public下的网页静态文件

    mvn compile -P build-public

3. 将静态文件copy到target中

   mvn prepare-package