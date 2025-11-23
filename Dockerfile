FROM eclipse-temurin:17-jdk-jammy

# Create working directory
WORKDIR /app

# Copy Spring Boot JAR
COPY target/zoo-app-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
