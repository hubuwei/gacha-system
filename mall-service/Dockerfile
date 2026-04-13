# ============================================
# Mall Service Dockerfile
# ============================================

# 阶段1: 构建
FROM maven:3.8-openjdk-11 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
COPY common/pom.xml common/
COPY mall-service/pom.xml mall-service/

RUN cd common && mvn dependency:go-offline -B && \
    cd ../mall-service && mvn dependency:go-offline -B

# 复制源代码并编译
COPY common/src common/src
COPY mall-service/src mall-service/src

RUN cd common && mvn clean install -DskipTests && \
    cd ../mall-service && mvn clean package -DskipTests

# 阶段2: 运行
FROM openjdk:11-jre-slim

WORKDIR /app

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 从构建阶段复制JAR包
COPY --from=builder /app/mall-service/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/actuator/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILE:-prod}"]
