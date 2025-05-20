#Build stage
FROM maven:3.9.9-eclipse-temurin-23-alpine AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY mvnw.cmd .
COPY bot bot
ENV SPRING_CONFIG_LOCATION=file:/app/bot/src/main/resources/application.yaml

RUN chmod +x ./mvnw
RUN sed -i 's/\r$//' mvnw

RUN cd bot && bash /app/mvnw dependency:go-offline
RUN --mount=type=cache,target=/root/.m2,rw cd bot && bash /app/mvnw verify -B -DskipTests=true

#Package stage
FROM openjdk:23-jdk-slim
WORKDIR /app

COPY --from=builder /app/bot/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
