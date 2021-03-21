# SPRING-BOOT CAMEL BOILERPLATE

## Development Pre-Requisites
- **IDE**: to develop and debug the application
- **Java 11+**: the programming language for this project is Java
- **Maven**: used to manage packages and build the project
- **Docker**: to avoid configuring development environment
- **Docker-Compose**: to create the infrastructure boilerplate

## Docker

This project contains a Dockerfile / Docker Compose that creates all the required infraestructure to develop the project:

- **Kafka**: event streaming platform;
- **Zookeeper**: keeps track of status of the Kafka cluster nodes and it also keeps track of Kafka topics, partitions etc;
- **PostgreSQL**: database used to load the data extracted from Kafka;
- **Camel Boilerplate**: the application itself.

**Start-Up Containers**  
`$ docker-compose up`  
**Shutdown Containers**  
`$ docker-compose down`

### Build Note

When you run the project using Docker, it compiles the source code and creates
a target folder containing the project's jar. After that you will not be able
to compile the project using your IDE or your command line because the
folder's owner now is Docker's user.

To solve this problem, just delete the target folder using sudo or
administrative privileges when you want to run the application using your
IDE or command line.

### Sending events to Kafka
To be able to send events to Kafka, you can connect into it's container and send events using the CLI. 
To do so, go into your terminal with the containers running and type the following commands to connect into the 
Kafka's container:

    $ docker exec -it camel_boilerplate_kafka sh
    $ cd /opt/kafka/bin

Inside Kafka's bin folder there's a sh script named `kafka-console.producer.sh`. 
This script allows you to create a producer and send events to the running instance on Kafka. Run the following command 
to connect into the topic *sample_topic*.

    $ kafka-console-producer.sh --topic sample_topic --bootstrap-server localhost:9092

You should see your cursor after a `>` char if you've been connected successfully. Now everything that you type and 
hit enter inside the console will produce a new event in the Kafka broker. You can use the following data as event 
model to test the application:

**Sample Event**

    {"name":"Bruno","quantity":12,"price":12.5}

## Running at local machine

This project contains a maven plugin to execute the application. If you want to run it locally 
without containers just execute the following command:

    $ mvn clean install spring-boot:run
