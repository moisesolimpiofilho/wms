FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY warehouse_module/pom.xml warehouse_module/pom.xml
COPY warehouse_module/src warehouse_module/src
RUN mvn -B -f warehouse_module/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/warehouse_module/target/warehouse_module-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
