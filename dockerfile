# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /usr/src/app
# Copy POM first for dependency caching
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy built application from build stage
COPY --from=build /usr/src/app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /usr/src/app/target/quarkus-app/*.jar /app/
COPY --from=build /usr/src/app/target/quarkus-app/app/ /app/app/
COPY --from=build /usr/src/app/target/quarkus-app/quarkus/ /app/quarkus/

ENV PRINT_ENV=true


EXPOSE 8080
CMD ["java", "-jar", "quarkus-run.jar"]