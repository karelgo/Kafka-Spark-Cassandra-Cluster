import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, Minutes, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe


object SparkWordCount {
   def main(args: Array[String]) {
      // if (args.length < 2) {
      //    System.err.println("Usage: KafkaWordCount <spark master> <port>")
      //    System.exit(1)
      // }

      val conf = new SparkConf().setAppName("KafkaWordCount").setMaster("spark://spark-master:7077")
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
      val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _).reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)      
      wordCounts.print()
      
      ssc.checkpoint("checkpoint")
      ssc.start()
      ssc.awaitTermination()
   }
}