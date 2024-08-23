# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies only
COPY pom.xml ./
COPY src ./src

# Download dependencies and build the application
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy only the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
