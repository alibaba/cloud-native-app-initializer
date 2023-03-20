FROM openjdk:17
WORKDIR /app
COPY initializer-generator/target/initializer-start-0.8.jar /app
COPY initializer-generator/target/classes/static /app

EXPOSE 7001
CMD ["java","-jar","/app/initializer-start-0.8.jar"]