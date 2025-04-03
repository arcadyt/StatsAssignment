FROM maven:3.9-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:20-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]