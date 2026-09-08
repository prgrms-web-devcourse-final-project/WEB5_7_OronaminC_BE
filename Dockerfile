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

ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]

