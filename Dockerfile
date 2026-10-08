FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY .. .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copia el JAR de la etapa 'build' a la etapa actual
COPY --from=build /app/target/lab1arquisoft.jar lab1arquisoft.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","lab1arquisoft.jar"]