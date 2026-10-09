FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY .. .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copia el JAR de la etapa 'build' a la etapa actual
COPY --from=build /app/target/lab1ArquiSoft.jar lab1ArquiSoft.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","lab1ArquiSoft.jar"]