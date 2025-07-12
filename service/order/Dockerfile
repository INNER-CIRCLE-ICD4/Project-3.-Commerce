# service/order/Dockerfile
FROM openjdk:21-jre-slim

WORKDIR /app

# JAR 파일 복사 (멀티 모듈 구조에서 빌드된 JAR)
COPY service/order/build/libs/*.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# 메모리 최적화 JVM 옵션
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]