FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiar el JAR correctamente desde la carpeta target
COPY FileService/target/FileService-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
