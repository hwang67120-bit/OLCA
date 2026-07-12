FROM gradle:jdk21-jammy AS build

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle ./

RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --system --create-home --uid 10001 olca

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

USER olca
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
