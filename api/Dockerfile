# Stage 1: Build the application using Gradle
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Stage 2: Package and run with a lightweight JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]