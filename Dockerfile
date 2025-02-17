FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew && \
    ./gradlew clean build -x test

FROM tomcat:10.1.28-jdk21
WORKDIR /usr/local/tomcat/webapps

COPY --from=build /app/build/libs/*.war ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
