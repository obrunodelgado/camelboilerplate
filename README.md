# SPRING-BOOT CAMEL BOILERPLATE

This is a Spring-Boot [Camel](https://camel.apache.org/manual/latest/faq/what-is-camel.html) 
Application model that you can use as a reference to study or even to use in your company. 
It contains a [Sample Route](https://github.com/obrunodelgado/camelboilerplate/blob/main/src/main/java/com/delgado/bruno/boilerplates/camel/routes/SampleRoute.java) that consumes events from a [Kafka](https://www.youtube.com/watch?v=06iRM1Ghr1k) 
Topic called *sample_topic*, process the data and saves it into a PostgreSQL database.

In this project you will find references for the following study topics:

- Spring-Boot
- Camel
- Kafka
- Docker
- Docker-Compose
- Schema Migrations
- Unit Tests
- Health Checks
- Liveness and Readiness probes


## Development Pre-Requisites
- **IDE**: to develop and debug the application
- **Java 11**: the programming language for this project is Java
- **Maven**: used to manage packages and build the project
- **Docker**: to avoid configuring development environment
- **Docker-Compose**: to create the infrastructure boilerplate

## Running the application

This project is configured to run using a 
[*docker-compose*](https://github.com/obrunodelgado/camelboilerplate/blob/main/docker-compose.yml) file or even at the 
command-line. The project is configured to use different configurations according to the environment which the project is being run.

> Running the project with Docker uses different environment variables than running
> it from the command-line.

I highly recommend you to run this project using Docker instead of running locally otherwise you will need to guarantee 
that you have a Kafka and a PostgreSQL instance running or locally or remotely so the application can connect to it and 
do it's job. 

> Running the application without Kafka and PostgreSQL instances will not result in Runtime Errors, but 
> will maintain the application in a "trying to recover connections" state. 

## Running using Docker

This project contains a Dockerfile / Docker Compose that creates all the required infraestructure to develop and 
test the project:

- **Kafka**: event streaming platform;
- **Zookeeper**: keeps track of status of the Kafka cluster nodes and it also keeps track of Kafka topics, partitions etc;
- **PostgreSQL**: database used to load the data extracted from Kafka;
- **Camel Boilerplate**: the application itself.

**Start-Up Containers**  
`$ docker-compose up`  
**Shutdown Containers**  
`$ docker-compose down`

Note in the [`application-docker.properties`](https://github.com/obrunodelgado/camelboilerplate/blob/main/src/main/resources/application-docker.properties)
that we have specific configurations to run the project using 
Docker envinroment. More information in the Environment Variables topic.

#### Build Note

When you run the project using Docker, it compiles the source code and creates
a target folder containing the project's jar. After that you will not be able
to compile the project using your IDE or your command line because the
folder's owner now is Docker's user.

To solve this problem, just delete the target folder using sudo or
administrative privileges when you want to run the application using your
IDE or command line.

## Running at local machine

This project contains a maven plugin to execute the application.
If you want to run it locally it is important that you guarantee
that there's a Kafka and a PostgreSQL running in localhost. 

You also should your Operational System's hosts file - that generally is located in `/etc/hosts` - and
add the following line to it.

    127.0.0.1   kafka

After this you just need to run the following maven command:

    $ mvn clean install spring-boot:run -Dspring-boot.run.arguments=--database.password=postgres -Dspring-boot.run.profiles=local

The `-Dspring-boot.run` command-line argument is defining a system variable called `database.password` avoiding to expose 
production credentials in the repository. The `-Dspring-boot.run.profiles` tells Spring-Boot that it must read specific 
local configurations from the `application-local.properties` file.

> If you have a Kafka and a PostgreSQL instance running somewhere else, you should change the `application-local.properties` 
> to point to it's hosts. Don't forget to change the `database.password` value in the command-line.

## Sending events to Kafka
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

    {"name":"Product","quantity":12,"price":12.5}

## Environment Variables
All real applications contains different configurations for different environments. In other words you will always have 
different connection strings for the Production and Local environment, for example. At Spring it is 
called *[profiles](https://docs.spring.io/spring-boot/docs/1.2.0.M1/reference/html/boot-features-profiles.html#boot-features-profiles)*.

In our case we need to set different hosts to run the project using the Docker environment and the local environment. 
This happens because each module of this application will run in 4 different containers, one for the application, one 
for the Zookeeper, one for the Kafka and another one for the PostgreSQL. Each of these containers "have it's own 
localhost definitions", meaning that localhost for the application if one host that is differente for Kafka.

In other words, when you are running the application using a Docker container and you configure it to connect into 
Kafka using a localhost definition it will not be able to connect to Kafka because there's not a Kafka running at the 
Application container because Kafka is in another container, another 'virtual machine'.

To solve this problem we need to tell the Application that Kafka is hosted at [`camel_boilerplate_kafka`](https://github.com/obrunodelgado/camelboilerplate/blob/main/docker-compose.yml#L15) - Kafka's 
container name - and the PostgreSQL is hosted at [`camel_boilerplate_postgres`](https://github.com/obrunodelgado/camelboilerplate/blob/main/docker-compose.yml#L31) - PostgreSQL container name.  These hosts 
are defined at the `docker-compose.yml` file.

So if we want to run the application in different environments we need to configure it to know how to manage this 
different environments. In the project we have 3 properties  files:

- [**application.properties**](https://github.com/obrunodelgado/camelboilerplate/blob/main/src/main/resources/application.properties):  properties that have the same values for any environment;
- [**application-docker.properties**](https://github.com/obrunodelgado/camelboilerplate/blob/main/src/main/resources/application-docker.properties): properties that have specific configuration values for Docker environment;
- [**application-local.properties**](https://github.com/obrunodelgado/camelboilerplate/blob/main/src/main/resources/application-local.properties): properties that have specific configuration values for Local environment;

The next step is to tell the application which property file it should consider other than the `application.properties` 
file and in this case we do it using a special command-line argument `-Dspring-boot.run.profiles={environment}` 
having the `{environment}` part replaced by `docker` or `local` string. For example: `-Dspring-boot.run.profiles=docker` 
or `-Dspring-boot.run.profiles=local`.

A last consideration to have about Environment Variables is that I ommited the database password value from any 
configuration file because you should not save credentials in your code repository for security reasons. That's why we 
are passing the command-line argument `-Dspring-boot.run.arguments=--database.password=postgres` when we execute the 
application: we are telling to Spring-Boot that the value for `database.password` property is `postgres`

> While defining credentials will open a security issue for anyone who have access to your code repository, defining 
> credentials in the command-line or event using Operational System variables will expose your credentials to anyone 
> who has access to the server machine. There's better approaches to secure your application but it would make this 
> Camel sample even more complex.


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
