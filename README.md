This is a TTU university project completed by me - a Twitter API Java project

Please test the web application here: http://dijkstra.cs.ttu.ee/~Kaspar.Metsa/twitter-app/

*For testing*: 

Insert the location(Tallinn, Frankfurt etc) and keyword to look for inside the tweets(Jazz, ukip, sheeran etc)

The app might not be working because the school server does frequent restarts.
Every time after restart I need to enter these commands:

export PATH=$PATH:/home/Kaspar.Metsa/apache-maven-3.3.9/bin
export JAVA_HOME=/home/Kaspar.Metsa/jdk1.8.0_91/jre/
cd /home/Kaspar.Metsa/HW2
mvn clean compile exec:java

Requirements for the Twitter app: (all these features are working)
==============
* You can select any location name(ie Tallinn, Frankfurt)
* The Java application asks for coordinates(latitude, longitude) from OpenStreetMap and then calculates the radius to search tweets in
* The Java application then gives those coordinates and radius to Twitter for the area to look for tweets in
* The Twitter API returns the tweets are they are displayed to the user
* All locations and their coordinates are saved into .csv file so the Java program doesn't ask coordinates from OpenStreetMap multiple times if they already exist in the .csv file
* You can sort the tweets based on author, date or contents of tweets
