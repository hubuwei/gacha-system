# ============================================
# Auth Service Dockerfile
# ============================================

# 阶段1: 构建
FROM maven:3.8-openjdk-11 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/

# 先安装父pom，再构建并安装common模块，最后处理auth-service
RUN mvn install -N -B && \
    cd common && mvn clean install -DskipTests -B && \
    cd ../auth-service && mvn dependency:go-offline -B

COPY common/src common/src
COPY auth-service/src auth-service/src

RUN cd common && mvn clean install -DskipTests && \
    cd ../auth-service && mvn clean package -DskipTests

# 阶段2: 运行
FROM openjdk:11-jre-slim

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=builder /app/auth-service/target/*.jar app.jar

RUN mkdir -p /app/logs /app/uploads/avatars && \
    chown -R appuser:appuser /app

USER appuser

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
