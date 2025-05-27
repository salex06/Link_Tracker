#Build stage
FROM openjdk:23-jdk-slim AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY mvnw.cmd .
COPY scrapper scrapper
ENV SPRING_CONFIG_LOCATION=file:/app/scrapper/src/main/resources/application.yaml

RUN chmod +x ./mvnw
RUN sed -i 's/\r$//' mvnw

RUN cd scrapper && bash /app/mvnw dependency:go-offline
RUN --mount=type=cache,target=/root/.m2,rw cd scrapper && bash /app/mvnw verify -B -DskipTests=true

#Package stage
FROM openjdk:23-jdk-slim
WORKDIR /app

COPY --from=builder /app/scrapper/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
