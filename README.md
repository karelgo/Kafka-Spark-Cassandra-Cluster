# KPMG-Data-Challenge

This folder contains files that create a pub/sub system using Apache Kafka and Spark

####PLAN:
1. Run Kafka, Spark, and Cassandra locally.
2. Transport local setup to Docker containers
3. Run in Docker

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

### To run a word count app in Spark run:

```docker exec -ti $(docker-compose ps -q spark-worker) sh```

and once inside the docker container shell, run:

```/spark/bin/spark-submit --master $SPARK_MASTER local/WordCount-assembly-1.0.jar```

todo: combine these two commands in one (buggy atm)

### To clean Docker run (works in Mac/UNIX terminal):

```docker rm -f -v $(docker ps -aq) 2>/dev/null; docker rmi $(docker images -qf "dangling=true") 2>/dev/null; docker rmi $(docker images | grep "dev-" | awk "{print $1}") 2>/dev/null; docker rmi $(docker images | grep "^<none>" | awk "{print $3}") 2>/dev/null```

IN PROGRESS

TODO: 
1. Implement consumer app in spark that subscribes to kafka topic
2. Store consumed data to Cassandra database
3. Enhance app functionality