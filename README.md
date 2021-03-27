# SPRING-BOOT CAMEL BOILERPLATE

## Development Pre-Requisites
- **IDE**: to develop and debug the application
- **Java 11**: the programming language for this project is Java
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

**Attention**: As you are running the project using containers, the application will 
not find the Kafka Broker neither the PostgreSQL if you define it's hosts as localhost. 
Make sure that in the `DependencyInjections` class the following constant definitions 
uncommented: 

    KAFKA_HOST = "camel_boilerplate_kafka"
    POSTGRESQL_HOST = "camel_boilerplate_postgres"    

### Java Build Note in Docker

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

This project contains a maven plugin to execute the application.
If you want to run it locally it is important that you guarantee
that there's a Kafka and a PostgreSQL running in localhost. Go to 
`DependencyInjections` class and make sure you have the following 
constant definitions uncommented:

    KAFKA_HOST = "localhost"
    POSTGRESQL_HOST = "localhost"    

Edit your hosts file that generally is located in `/etc/hosts` and
add the following line to it.

    127.0.0.1   kafka

After this you just need to run the following maven command:

    $ mvn clean install spring-boot:run

## Health Checks

This project is configured with some endpoints in order to inform the state of the application.

Basically we have this endpoints:

- **/actuator**: a summary of the available health endpoints
- **/actuator/health**: general overview of the health status of the project and it's components.
- **/actuator/health/liveness**: tells whether the internal state is valid. If Liveness is broken, this means that the application itself is in a failed state and cannot recover from it.
- **/actuator/health/readiness**: tells whether the application is ready to accept requests. It will be defined 
  as out of service if Kafka goes down.
- **/actuator/metrics**: a list of all the available metrics of the application
- **/actuator/metrics/{metric_name}**:  metric details

A health application should return the following json when you make a GET request to `/actuator/health`:

    {
       "status":"UP",
       "components":{
          "camelHealth":{
             "status":"UP",
             "details":{
                "name":"camel-health-check",
                "context":"UP",
                "route:sample":"UP"
             }
          },
          "db":{
             "status":"UP",
             "details":{
                "database":"PostgreSQL",
                "validationQuery":"isValid()"
             }
          },
          "diskSpace":{
             "status":"UP",
             "details":{
                "total":254356226048,
                "free":170402906112,
                "threshold":10485760,
                "exists":true
             }
          },
          "kafka":{
             "status":"UP"
          },
          "livenessState":{
             "status":"UP"
          },
          "ping":{
             "status":"UP"
          },
          "readinessState":{
             "status":"UP"
          }
       },
       "groups":[
          "liveness",
          "readiness"
       ]
    }

If Kafka or the database goes offline, the kafka property in the health json should looks like this:

    "db": {
      "status": "DOWN",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "kafka": {
      "status": "DOWN",
      "details": {
        "Reason": "Kafka may be offline"
      }
    }

As soon as Kafka goes online again, the application will recover it's state.
