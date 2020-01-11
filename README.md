# LordSith
An ogame bot using docker. 

# How to use it: 
-Edit the file application.properties and put your account's data. Email, password, and url of your server. 
-Compile the project using maven: 
mvn clean install. 

If you want to use it on a machine without graphical interface, use the pro profile:
mvn -Ppro clean install

-Run the project. java -jar LordSith/lordsith/target/lordsith-0.0.1-SNAPSHOT.jar

-The boot use 3 cronjobs that you can find on class Tasks.java. The main job executes itself every 20 minutes from 6 to 21. So there you can set the frecuency. 

If you want to use docker, then use the pro profile. And execute this.
docker build -t lord-sith .
docker run lord-sith
