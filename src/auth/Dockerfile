FROM amazoncorretto:17.0.12-al2023
EXPOSE 8081

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

HEALTHCHECK --interval=5s --timeout=1s --start-period=10s --retries=10 \
    CMD curl -I http://localhost:8081