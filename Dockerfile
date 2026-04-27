# Use a lightweight Java 21 Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Wildcard ensures it finds the jar regardless of versioning
COPY target/*.jar app.jar

# Standard port, but remember we made this configurable in the properties!
EXPOSE 8080

# This ensures the container starts with the PORT env var if provided
ENTRYPOINT ["java", "-jar", "app.jar"]
