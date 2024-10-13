# Stage 1 - package with Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 - deploy
FROM amazoncorretto:17.0.12-al2023
EXPOSE 8081

ARG JAR_FILE=app/target/*.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

HEALTHCHECK --interval=5s --timeout=1s --start-period=10s --retries=10 \
    CMD curl -I http://localhost:8081