# Use a Maven + JDK 17 image to build the project
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies first (caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy all source code
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests

# Use a lightweight JDK image for running the jar
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app uses
EXPOSE 8081

# Set environment variables for MPESA credentials (set these in Render dashboard)
ENV MPESA_CONSUMER_KEY="NPNBTnG1VhJ1Sh4oFZPFGpREyJ9vtNS68TJKnn9iVZ2dw5JW"
ENV MPESA_CONSUMER_SECRET="eGKavExMsiZFhJ3Apiar1iY636U5wvXcTsdFYWzaATv6OmHlKsf9TMVi5tChMyuy"

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
