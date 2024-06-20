FROM maven:3.9.6-eclipse-temurin-17
COPY src src
COPY pom.xml ./
RUN mvn clean install -f ./

# PACKAGE STAGE
#
COPY target/file-upload-0.0.2-SNAPSHOT.jar file-upload-0.0.2-SNAPSHOT.jar
CMD ["java","-jar","file-upload-0.0.2-SNAPSHOT.jar"]