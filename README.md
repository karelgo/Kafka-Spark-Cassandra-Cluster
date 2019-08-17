# KPMG-Data-Challenge

This folder contains files to create a pub/sub system using Apache Kafka, Spark, and Cassandra.

For the next steps you must have Docker installed. (some commands only work on UNIX systems e.g. grep)

### To set up the kafka server, zookeeper, and spark cluster (with two workers and one master) in Docker, run:

```docker-compose up --build -d --scale node=1```

## To get into the Cassandra database and create a table, run:

```docker exec -ti $(docker ps | grep cassandra | awk '{print $1}') cqlsh``` 

and then

```CREATE KEYSPACE karelns WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 2}```

and then

```CREATE TABLE karelns.hashtagcount (hashtag text PRIMARY KEY, count int);```

### To run a real time twitter application producer, run:

```docker exec -ti $(docker ps | grep client | awk '{print $1}') sh```

```cd /local/apps && java -cp "/opt/kafka_2.11-2.3.0/libs/*":"/opt/twitter4j-4.0.7/lib/*":. KafkaTwitterProducer ZHtJeQLMOziJmZRhrSpwi5etd UDLqG5zzttnK2nV0FLZLMjvp79c0vxZkf1S2nN5R9cb4xdqqIX 943468482871558149-YIGUcdoUIpMHB7rj7lcFUCKv6KVrUYQ YBnZox3nvJ0FJbG61G1KpklOIBRdCJxReMUjqBjYtCoax events blockchain```

The word 'blockchain' is added to filter the tweets containing the word blockchain, the hashtag gets posted to the kafka topic, which then gets processed (hashtags are counted) in the spark cluster.

### To run a word count on the twitter data hashtags, on a spark cluster, run:

```GATEWAYEIP=$(docker network inspect kpmg-data-challenge_default | grep "Gateway" | awk '{print $2}' | bc -l)```

```docker exec -e GATEWAYEIP=$GATEWAYEIP -ti $(docker ps | grep client | awk '{print $1}') sh```

```/spark/bin/spark-submit --master $SPARK_MASTER /local/apps/SparkWordCount-assembly-1.0.jar $GATEWAYEIP karelns hashtagcount``` 

(I added the gateway ipv4 address to link to the cassandra host because all the containers are on the same bridge network)

### To check the data in the database, run:

```docker exec -ti $(docker ps | grep cassandra | awk '{print $1}') cqlsh```

and run

```SELECT * FROM karelns.hashtagcount```

TODO:
1. Enhance app functionality
2. Hide crypto material from plain sight (not that it matters a lot, just want to show I am aware I am showing the twitter keys)

### To clean Docker run:

```docker rm -f -v $(docker ps -aq) 2>/dev/null; docker rmi $(docker images -qf "dangling=true") 2>/dev/null; docker rmi $(docker images | grep "dev-" | awk "{print $1}") 2>/dev/null; docker rmi $(docker images | grep "^<none>" | awk "{print $3}") 2>/dev/null```
