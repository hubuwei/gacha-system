# ============================================
# Game Service Dockerfile
# ============================================

FROM maven:3.8-openjdk-11 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY game-service/pom.xml game-service/

# 先安装父pom，再构建并安装common模块，最后处理game-service
RUN mvn install -N -B && \
    cd common && mvn clean install -DskipTests -B && \
    cd ../game-service && mvn dependency:go-offline -B

COPY common/src common/src
COPY game-service/src game-service/src

RUN cd common && mvn clean install -DskipTests && \
    cd ../game-service && mvn clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=builder /app/game-service/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
