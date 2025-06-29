# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /usr/src/app

COPY . .

# Build the app using Quarkus
RUN mvn clean install -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /usr/src/app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /usr/src/app/target/quarkus-app/*.jar /app/
COPY --from=build /usr/src/app/target/quarkus-app/app/ /app/app/
COPY --from=build /usr/src/app/target/quarkus-app/quarkus/ /app/quarkus/

# Expose port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "quarkus-run.jar"]