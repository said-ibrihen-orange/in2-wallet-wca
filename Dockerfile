# temp build
FROM docker.io/gradle:8.0.2-jdk AS TEMP_BUILD
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

# build image
FROM openjdk:17
WORKDIR /app
COPY --from=TEMP_BUILD /home/gradle/src/service-matrix.properties /app/
COPY --from=TEMP_BUILD /home/gradle/src/configs /app/configs
COPY --from=TEMP_BUILD /home/gradle/src/build/libs/*.jar /app/
ENTRYPOINT ["java", "-jar", "/app/wallet-api-1.5.0.jar"]
