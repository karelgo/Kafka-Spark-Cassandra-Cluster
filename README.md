# KPMG-Data-Challenge

This folder contains files that create a pub/sub system using apache kafka. 

Spark is used to work with this pub/sub system.

To set up the kafka server, zookeeper, and spark cluster, run:

```docker-compose up --build -d```

To run multiple workers (e.g. 2) run:

```docker-compose up --build -d --scale spark-worker=2```

IN PROGRESS

