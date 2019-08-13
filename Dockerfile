## Dockerfile for spark cluster with kafka and twitter4j libs

FROM openjdk:8-alpine

RUN apk --update add wget tar bash curl unzip

RUN wget http://apache.40b.nl/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz

RUN tar -xzf spark-2.4.3-bin-hadoop2.7.tgz && \
    mv spark-2.4.3-bin-hadoop2.7 /spark && \
    rm spark-2.4.3-bin-hadoop2.7.tgz

COPY twitter4j-4.0.7 /opt/twitter4j-4.0.7
COPY kafka_2.11-2.3.0 /opt/kafka_2.11-2.3.0
COPY start-master.sh /start-master.sh
COPY start-worker.sh /start-worker.sh
