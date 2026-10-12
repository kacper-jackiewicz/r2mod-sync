FROM maven:3.9.11-eclipse-temurin-25-alpine AS app-build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests package

FROM golang:1.21.3-alpine AS r2modman-build
RUN CGO_ENABLED=0 go install github.com/mpawlowski/r2modman-headless@latest

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=r2modman-build /go/bin/r2modman-headless /usr/local/bin/r2modman-headless
COPY --from=app-build /app/backend/target/r2mod-sync.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]