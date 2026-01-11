FROM eclipse-temurin:17-jdk as build
WORKDIR /app
COPY . ./
RUN apt-get update && apt-get install -y findutils
RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
