# Stage 1: Build the application using Maven and Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime image using Java 17 JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]