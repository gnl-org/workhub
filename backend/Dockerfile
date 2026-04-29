# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy maven files first for better layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

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