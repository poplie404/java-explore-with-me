FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y wget \
    && wget https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh \
    && chmod +x wait-for-it.sh \
    && rm -rf /var/lib/apt/lists/*

# ✅ Maven = target/*.jar (НЕ build/libs!)
COPY target/ewm-main-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "./wait-for-it.sh main-db:5432 --timeout=60 -- ./wait-for-it.sh stats-server:9090 --timeout=60 -- java -jar app.jar"]
