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
FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=builder /app/auth-service/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/uploads/avatars /app/logs

# 设置权限
RUN chmod -R 755 /app

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
