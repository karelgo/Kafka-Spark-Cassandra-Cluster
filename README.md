# KPMG-Data-Challenge

This folder contains files to create a pub/sub system using Apache Kafka, Spark, and Cassandra.

For the next steps you must have Docker installed.

### To set up the kafka server, zookeeper, and spark cluster in Docker, run:

```docker-compose up --build -d```

To run multiple spark workers (e.g. 2) run:

```docker-compose up --build -d --scale spark-worker=2```

### To run a kafka producer and consumer in Docker run (you might have to wait ~30 seconds for the docker containers to start up (or longer if images need to be pulled)):

In one terminal run:

```docker exec -it $(docker-compose ps -q kafka) kafka-console-producer.sh --broker-list localhost:9092 --topic events```

Now add text you want to see produced in the consumer.

In another terminal run:

```docker exec -it $(docker-compose ps -q kafka) kafka-console-consumer.sh  --bootstrap-server localhost:9092 —topic events --whitelist events --from-beginning```

You should now see the text you typed in the producer shell.

### To run a real time twitter application producer run:

```docker exec -ti $(docker-compose ps -q spark-worker) sh```

then run

```javac -cp "/opt/kafka_2.11-2.3.0/libs/*":"/opt/twitter4j-4.0.7/lib/*":. /local/KafkaTwitterProducer.java```

then run

```cd local```

then run

```java -cp "/opt/kafka_2.11-2.3.0/libs/*":"/opt/twitter4j-4.0.7/lib/*":. KafkaTwitterProducer ZHtJeQLMOziJmZRhrSpwi5etd UDLqG5zzttnK2nV0FLZLMjvp79c0vxZkf1S2nN5R9cb4xdqqIX 943468482871558149-YIGUcdoUIpMHB7rj7lcFUCKv6KVrUYQ YBnZox3nvJ0FJbG61G1KpklOIBRdCJxReMUjqBjYtCoax events kpmg ibm big-data kafka hadoop scala java```

### To run a word count on a spark cluster that consumes the twitter data run:

IN PROGRESS

TODO: 
1. Deploy word count app on spark cluster to read and do a word count on the Twitter data.
2. Store Twitter data to Cassandra database
3. Enhance app functionality
4. Hide crypto material from plain sight (not that it matters a lot, just want to show I am aware I am showing the twitter keys)

### To clean Docker run (works in Mac/UNIX terminal):

```docker rm -f -v $(docker ps -aq) 2>/dev/null; docker rmi $(docker images -qf "dangling=true") 2>/dev/null; docker rmi $(docker images | grep "dev-" | awk "{print $1}") 2>/dev/null; docker rmi $(docker images | grep "^<none>" | awk "{print $3}") 2>/dev/null```
