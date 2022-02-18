FROM docker.io/maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY src src
COPY pom.xml pom.xml
COPY res res
RUN mvn package assembly:single

FROM docker.io/openjdk:17-alpine
WORKDIR /app
COPY --from=builder /app/target/Langton-s-Ant-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./langtonclient.jar
CMD ["java", "-jar", "langtonclient.jar", "--nogui"]
