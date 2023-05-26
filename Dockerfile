
FROM gcr.io/distroless/java17:latest
COPY ./target/\*.jar /usr/app/my-app.jar


ENTRYPOINT ["java","-jar","/usr/app/my-app.jar"]  


# FROM eclipse-temurin:17-jdk-focal
 
# WORKDIR /app
 
# COPY .mvn/ .mvn
# COPY mvnw pom.xml ./
# RUN ./mvnw dependency:go-offline
 
# COPY src ./src
 
# CMD ["./mvnw", "spring-boot:run"]