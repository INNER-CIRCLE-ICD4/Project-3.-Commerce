# infra/eureka-server/Dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY infra/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
