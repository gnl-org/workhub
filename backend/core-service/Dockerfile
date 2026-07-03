# Stage 1: Build using standard Maven (No local wrapper needed)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml to download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy your source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run as non-root for security
RUN useradd -m workhubuser
USER workhubuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]