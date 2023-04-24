### Configuration

### set --build-args SKIP_TESTS=true to use 
ARG SKIP_TESTS

# --- dos2unix-env    # convert line endings from Windows machines
FROM docker.io/rkimf1/dos2unix@sha256:60f78cd8bf42641afdeae3f947190f98ae293994c0443741a2b3f3034998a6ed as dos2unix-env
WORKDIR /convert
COPY gradlew .
RUN dos2unix ./gradlew

# --- build-env
FROM docker.io/gradle:8.0.2-jdk as build-env
ARG SKIP_TESTS
WORKDIR /appbuild
COPY . /appbuild
COPY --from=dos2unix-env /convert/gradlew .
VOLUME /home/gradle/.gradle
RUN if [ -z "$SKIP_TESTS" ]; \
    then echo "* Running full build" && gradle -i clean build installDist; \
    else echo "* Building but skipping tests" && gradle -i clean installDist -x test; \
    fi

# --- application-dev
FROM openjdk:17 AS app-env
WORKDIR /app
COPY --from=build-env /appbuild/service-matrix.properties /app/
COPY --from=build-env /appbuild/configs /app/configs
COPY --from=build-env /appbuild/build/libs/in2-dome-wallet-backend-0.2.0-SNAPSHOT.jar /app/
RUN bash -c 'touch /app/in2-dome-wallet-backend-0.2.0-SNAPSHOT.jar'
ENTRYPOINT ["java","-jar","/app/in2-dome-wallet-backend-0.2.0-SNAPSHOT.jar"]
