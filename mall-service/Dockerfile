# ============================================
# Mall Service Dockerfile
# ============================================

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
