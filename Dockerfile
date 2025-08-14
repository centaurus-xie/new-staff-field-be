FROM openjdk:17-jdk-slim

WORKDIR /app

# 构建时复制jar包（需要先本地构建）
COPY target/*.jar app.jar

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
