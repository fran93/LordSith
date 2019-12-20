FROM arm64v8/alpine:edge
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Add openjdk8
RUN apk update
RUN apk add openjdk8

# And of course we need Firefox if we actually want to *use* GeckoDriver
RUN apk add --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing firefox
# Get all the prereqs
RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-2.30-r0.apk
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.30-r0/glibc-bin-2.30-r0.apk
RUN apk add glibc-2.30-r0.apk
RUN apk add glibc-bin-2.30-r0.apk

# Then install GeckoDriver
RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.26.0/geckodriver-v0.26.0-linux64.tar.gz
RUN tar -zxf geckodriver-v0.26.0-linux64.tar.gz -C /usr/bin

# Add non privileged user
RUN addgroup -S players && adduser -S player -G players

# Relaxing permissions
RUN chmod -R 777 /usr/bin/geckodriver

RUN export PATH=$PATH:/usr/bin/geckodriver

# Non root user.
USER player

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]