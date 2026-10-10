FROM golang:1.21.3-alpine AS r2modman-build
RUN CGO_ENABLED=0 go install github.com/mpawlowski/r2modman-headless@latest

FROM eclipse-temurin:27-jre-alpine
WORKDIR /app
COPY --from=r2modman-build /go/bin/r2modman-headless /usr/local/bin/r2modman-headless
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]