# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Package the application
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build /app/target/bookcollection-0.0.1-SNAPSHOT.jar /book-app.jar
WORKDIR /
ENTRYPOINT ["java","-jar","/book-app.jar"]

# Optional: Set environment variables
ENV JAVA_OPTS=""

# Optional: Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s CMD curl --fail http://localhost:8080/actuator/health || exit 1
