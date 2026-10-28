FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src src
RUN mvn package -DskipTests -q
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/time-manager-0.0.1.jar app.jar
RUN mkdir -p /app/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
