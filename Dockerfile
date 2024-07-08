FROM eclipse-temurin:21.0.3_9-jre-jammy
COPY build/libs/TruExercise-*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar", "/app.jar"]
EXPOSE 8080