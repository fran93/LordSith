FROM openjdk:12-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# And of course we need Firefox if we actually want to *use* GeckoDriver
RUN apk add firefox-esr

# Then install GeckoDriver
RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.25.0/geckodriver-v0.25.0-linux64.tar.gz
RUN tar -zxf geckodriver-v0.25.0-linux64.tar.gz -C /usr/bin

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]