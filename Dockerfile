# Estágio 1: Build da aplicação com Maven e JDK 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copia o pom.xml do subdiretório auth
COPY auth/pom.xml ./pom.xml

# Copia o código-fonte do subdiretório auth
COPY auth/src ./src

# Executa o build da aplicação no diretório /app
RUN mvn clean package -DskipTests

# Estágio 2: Execução com JRE 25
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copia o JAR gerado no estágio de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]