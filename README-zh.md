# 云原生应用脚手架
这是一个源自于 Spring Initializr 构建的云原生应用脚手架项目，它包含以下模块：
* initializer-generator: 脚手架生成项目模块，在其中`io.spring.start.site`目录下引用了部分 [start.spring.io](https://start.spring.io/) 的基础代码。
* initializer-page: 脚手架前端页面

## 基于源代码运行
请在本地 clone 该项目，并确保具备 Java 17 环境。

### 构建项目
在项目根目录，执行以下命令，安装 Node 和 Yarn：
```shell
mvn compile -P install-yarn
```
在项目根目录，执行以下命令，将静态文件 Copy 到 `initializer-generator` 模块的 target 中：
```shell 
mvn prepare-package
```

### 启动项目
进入`initializer-generator` 模块，执行以下命令启动应用：
```shell
cd initializer-generator
mvn spring-boot:run
```
在浏览器中，输入 http://127.0.0.1:7001/bootstrap.html 即可使用脚手架项目进行工程构建。

## 基于镜像运行
在进行后续操作前，请确保相关环境已经安装 Docker。

### 拉取镜像
在本地命令行执行以下命令，拉取脚手架工程镜像：
```shell
docker pull registry.cn-hangzhou.aliyuncs.com/cloud-native-app-initializer/initializer:latest
```

### 启动容器
在本地命令行执行以下命令，启动脚手架容器：
```shell
docker run -it -p 127.0.0.1:7001:7001 registry.cn-hangzhou.aliyuncs.com/cloud-native-app-initializer/initializer:latest
```

## 项目License
该项目是一个采用 [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html) 的项目。