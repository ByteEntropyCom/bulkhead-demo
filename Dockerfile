# Use Eclipse Temurin JRE 21 on Alpine Linux for a tiny, secure image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the executable jar from the target folder
# We use a wildcard (*) so it finds the jar regardless of the version number
COPY target/*.jar app.jar

# Open port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
