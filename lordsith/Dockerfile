FROM alpine:edge
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Add openjdk8
RUN apk update
RUN apk add openjdk8

# And of course we need Firefox if we actually want to *use* GeckoDriver
RUN apk add firefox-esr

# Then install GeckoDriver
RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-linux64.tar.gz
RUN tar -zxf geckodriver-v0.26.0-linux64.tar.gz -C /usr/bin

# Add non privileged user
RUN addgroup -S players && adduser -S player -G players
USER player

# Relaxing permissions for OpenShift and other non-sudo environments
RUN sudo chmod -R 777 /home/player
RUN sudo chgrp -R 0 /home/player
RUN sudo chmod -R g=u /home/player

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]