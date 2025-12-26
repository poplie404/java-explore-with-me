FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/ewm-main-service-*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
