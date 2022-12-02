# Cloud Native App Initializer
This is a Cloud Native App Initializer project derived from Spring Initializr, which includes the following modules:
* initializer-generator: Generate Project Modules, part of the basic code of [start.spring.io](https://start.spring.io/) is referenced in the `io.spring.start.site` directory.
* initializer-page: Front page

## Run from source
Please clone the project locally and make sure you have a Java 17 environment.

### Build project
In the project root directory, execute the following commands to install `Node` and `Yarn`:
```shell
mvn compile -P install-yarn
```
In the project root directory, execute the following command to copy the static files to the target of the `initializer-generator` module:
```shell
mvn prepare-package
```

### Run project
Enter the `initializer-generator` module and execute the following command to start the application:
```shell
cd initializer-generator
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