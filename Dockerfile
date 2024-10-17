FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY target/CopyHHFromParser-0.0.1-SNAPSHOT.jar /app/app.jar

FROM eclipse-temurin:17-jre
COPY --from=build /app/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
