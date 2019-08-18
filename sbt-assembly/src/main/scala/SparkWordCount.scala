import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, Minutes, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import com.datastax.spark.connector._
import org.apache.spark.sql.cassandra._

object SparkWordCount {
   def main(args: Array[String]) {

      if (args.length != 3) {
         System.err.println("Usage: SparkWordCount <cassandraHost> <cassandra namespace> <database name>")
         System.exit(1)
      }

      val cassandraHost = args(0)
      
      val conf = new SparkConf()
      conf.set("spark.cassandra.connection.host", cassandraHost)
      conf.setMaster("spark://spark-master:7077")
      conf.setAppName("KafkaWordCount")

      val ssc = new StreamingContext(conf, Seconds(1))
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
      val pairs = words.map(word => (word, 1))
      val wordCounts = pairs.reduceByKey(_ + _)

      def updateFunction(newValues: Seq[(Int)], runningCount: Option[(Int)]): Option[(Int)] = {
         var result: Option[(Int)] = null
         if (newValues.isEmpty){ //check if the key is present in new batch if not then return the old values
            result = Some(runningCount.get)
            }
         else {
            newValues.foreach { x => {// if we have keys in new batch ,iterate over them and add it
         if (runningCount.isEmpty){
            result = Some(x)// if no previous value return the new one
         } else {
            result = Some(x+runningCount.get) // update and return the value
            }
         } }
         }
         result
      }
      
      val runningCounts = wordCounts.updateStateByKey[Int](updateFunction(_,_))

      runningCounts.print()

      val nameSpace = args(1)
      val databaseName = args(2)

      runningCounts.foreachRDD((rdd) => {
         rdd.saveToCassandra(nameSpace, databaseName, SomeColumns("hashtag", "count"))
      })

   ssc.checkpoint("checkpoint")
   ssc.start()
   ssc.awaitTermination()
   }
}