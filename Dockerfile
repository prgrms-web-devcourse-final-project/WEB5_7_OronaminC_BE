FROM gradle:jdk21 as builder

WORKDIR /libs

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew build --no-daemon -x test


FROM openjdk:21-slim

WORKDIR /app

COPY --from=builder /libs/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]

