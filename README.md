[English](README.md) | [简体中文](README-zh.md)

---
# Cloud Native App Initializer

## Docs
- [Code Contribution](docs/CONTRIBUTING.md)

## Code structure
This is a Cloud Native App Initializer project derived from Spring Initializr, you can directly experience the function of the project through [start.aliyun.com](https://start.aliyun.com/), which includes the following modules:
* initializer-generator: Generate Project Modules, part of the basic code of [start.spring.io](https://start.spring.io/) is referenced in the `io.spring.start.site` directory.
* initializer-page: Front page

## Run from source
Please clone the project locally and make sure you have a Java 17 environment.

### Build project

Since the front end is stored in the form of source code in this project, it needs to be compiled with yarn to become a resource file of the current project in order to be accessed correctly. Execute in the `cloud-native-app-initializer` directory:
```shell
mvn clean install
```
After this step is executed, compile and build the project module. Among them, the compiled front-end files will be copied to the `initializer-page/target/classes/static` directory

### Run project
Enter the `initializer-start` module and execute the following command to start the application:
```shell
mvn spring-boot:run
```
In the browser, enter `http://127.0.0.1:7001/bootstrap.html` to use the initializer project for project bootstrap.

## Run based on Docker
Before performing subsequent operations, please ensure that Docker has been installed in the relevant environment.

### Pull image
Execute the following command on the local command line to pull the initializer project image:
```shell
docker pull registry.cn-hangzhou.aliyuncs.com/cloud-native-app-initializer/initializer:latest
```

### Start the container
Execute the following command on the local command line to start the initializer container:
```shell
docker run -it -p 127.0.0.1:7001:7001 registry.cn-hangzhou.aliyuncs.com/cloud-native-app-initializer/initializer:latest
```

## License
This project is a project under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
