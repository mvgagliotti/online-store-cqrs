# online-store-cqrs [WIP]
A event-sourcing &amp; CQRS online store, based on Akka
## How to run it
* You will need docker & docker-composed installed

* From root project directory, start cassandra db: 
  * docker-compose up
* Yet from root dir, build the project: 
  * gradle build
  * A jar file will show up at ./target/
* Run it: 
  * java -jar ./target/online-store.jar
