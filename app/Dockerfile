FROM maven:3.9.11-eclipse-temurin-21 AS builder
LABEL authors="filiperosa"
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-noble
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
