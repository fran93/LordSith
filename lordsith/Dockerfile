FROM alpine:edge
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Add openjdk8
RUN apk update
RUN apk add openjdk8

# And of course we need Firefox if we actually want to *use* GeckoDriver
RUN apk add --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing firefox

# Then install GeckoDriver
RUN wget https://github.com/d0ku/GeckoDriver_ARMv6/releases/download/v0.26.0/geckodriver
RUN mv geckodriver /usr/bin/geckodriver

# Add non privileged user
RUN addgroup -S players && adduser -S player -G players

# Relaxing permissions
RUN chmod -R 777 /usr/bin/geckodriver

# Add path
RUN export PATH=$PATH:/usr/bin/geckodriver

# Non root user.
USER player

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]