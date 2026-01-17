# ----- Build Stage -----
    FROM eclipse-temurin:17-jdk as build

    WORKDIR /app
    
    COPY mvnw .
    COPY .mvn .mvn
    COPY pom.xml .
    COPY src ./src
    
    RUN chmod +x mvnw
    RUN ./mvnw clean package -DskipTests
    
# ----- Runtime Stage -----
    FROM eclipse-temurin:17-jre
    
    # Install curl for healthchecks
    RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
    
    WORKDIR /app
    
    COPY --from=build /app/target/*.jar app.jar
    
    EXPOSE 8080
    
    ENTRYPOINT ["java", "-jar", "app.jar"]
    