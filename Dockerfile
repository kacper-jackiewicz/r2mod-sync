FROM golang:1.21.3-alpine AS r2modman-build
RUN CGO_ENABLED=0 go install github.com/mpawlowski/r2modman-headless@7a478c9e8ec6fb3cd1687a17fd47c84a7e14b24f

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=r2modman-build /go/bin/r2modman-headless /usr/local/bin/r2modman-headless
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]