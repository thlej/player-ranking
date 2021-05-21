ARG GRADLE_VERSION=7.0.2
ARG JAVA_VERSION=11

#build
FROM gradle:${GRADLE_VERSION}-jdk11 as build
COPY src src
COPY build.gradle.kts ./
COPY settings.gradle.kts ./
COPY gradle.properties ./
RUN gradle clean shadowJar

#ENTRYPOINT GRADLE_USER_HOME=cache gradle test jacocoTestReport --info --no-daemon

#release
FROM openjdk:${JAVA_VERSION}-jre-slim
COPY --from=build /home/gradle/build/libs/*.jar player-ranking.jar
ENTRYPOINT java -Xmx128m -XshowSettings:vm -jar /player-ranking.jar