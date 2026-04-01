# Giai đoạn 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Khởi tạo Cache Layer cho Dependency
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
