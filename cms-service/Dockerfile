FROM eclipse-temurin:11-jre

WORKDIR /app

COPY target/cms-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8085

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
