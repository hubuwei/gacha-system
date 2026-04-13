# ============================================
# Gacha Service Dockerfile
# ============================================

FROM maven:3.8-openjdk-11 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY gacha-service/pom.xml gacha-service/

RUN cd common && mvn dependency:go-offline -B && \
    cd ../gacha-service && mvn dependency:go-offline -B

COPY common/src common/src
COPY gacha-service/src gacha-service/src

RUN cd common && mvn clean install -DskipTests && \
    cd ../gacha-service && mvn clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=builder /app/gacha-service/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
