import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, Minutes, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import com.datastax.spark.connector._

object SparkWordCount {
   def main(args: Array[String]) {

      val conf = new SparkConf()
      conf.set("spark.cassandra.connection.host", "127.0.0.1") // check for right host
      conf.setMaster("spark://spark-master:7077")
      conf.setAppName("KafkaWordCount")

      val ssc = new StreamingContext(conf, Seconds(3))
      val topics = Array("events")

      val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "kafka:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "test-consumer-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
      )

      val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
      )

      val lines = stream.map(_.value)
      val words = lines.flatMap(_.split(" "))
      val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _).reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)      
      wordCounts.print()

      wordCounts.foreachRDD((rdd, time) => {
      rdd.cache()
      println("Writing " + rdd.count() + " rows to Cassandra")
      rdd.saveToCassandra("karel", "LogTest", SomeColumns("Hashtag", "Count"))
      })
      
      ssc.checkpoint("checkpoint")
      ssc.start()
      ssc.awaitTermination()
   }
}