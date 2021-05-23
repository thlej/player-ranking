ARG GRADLE_VERSION=7.0.2
ARG NODE_VERSION=14
ARG JAVA_VERSION=11

#build client
FROM node:${NODE_VERSION}-alpine as build_client
WORKDIR /usr/app
COPY client/package.json .
COPY client/package-lock.json .
COPY client .
RUN npm install
RUN npm install -g @angular/cli
RUN ng build --configuration="production"

#build server
FROM gradle:${GRADLE_VERSION}-jdk11 as build_server
COPY server/src src
COPY server/build.gradle.kts ./
COPY server/settings.gradle.kts ./
COPY server/gradle.properties ./
COPY --from=build_client /usr/app/dist/client src/main/resources/static
RUN gradle clean shadowJar

#release
FROM openjdk:${JAVA_VERSION}-jre-slim
COPY --from=build_server /home/gradle/build/libs/*.jar player-ranking.jar
ENTRYPOINT java -jar /player-ranking.jar