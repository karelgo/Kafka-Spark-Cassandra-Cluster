import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;
import twitter4j.conf.*;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaTwitterProducer {
   public static void main(String[] args) throws Exception {
      LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);
      
      if(args.length < 5){
         System.out.println(
            "Usage: KafkaTwitterProducer <twitter-consumer-key> <twitter-consumer-secret> <twitter-access-token> <twitter-access-token-secret> <topic-name> <twitter-search-keywords>");
         return;
      }
      
      String consumerKey = args[0].toString();
      String consumerSecret = args[1].toString();
      String accessToken = args[2].toString();
      String accessTokenSecret = args[3].toString();
      String topicName = args[4].toString();
      String[] arguments = args.clone();
      String[] keyWords = Arrays.copyOfRange(arguments, 5, arguments.length);

      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setDebugEnabled(true)
         .setOAuthConsumerKey(consumerKey)
         .setOAuthConsumerSecret(consumerSecret)
         .setOAuthAccessToken(accessToken)
         .setOAuthAccessTokenSecret(accessTokenSecret);

      TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
      
      FilterQuery query = new FilterQuery().track(keyWords);
      twitterStream.filter(query);

      Thread.sleep(5000);
      
      //Add Kafka producer config settings
      Properties props = new Properties();
      props.put("bootstrap.servers", "kafka:9092");
      props.put("acks", "all");
      props.put("retries", 0);
      props.put("batch.size", 16384);
      props.put("linger.ms", 1);
      props.put("buffer.memory", 33554432);
      
      props.put("key.serializer", 
         "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", 
         "org.apache.kafka.common.serialization.StringSerializer");
      
      Producer<String, String> producer = new KafkaProducer<String, String>(props);
      int i = 0;
      int j = 0;
      
      while(i < 1000) {
         Status ret = queue.poll();
         
         if (ret == null) {
            Thread.sleep(100);
            i++;
         }else {
            for(HashtagEntity hashtage : ret.getHashtagEntities()) {
               System.out.println("Hashtag: " + hashtage.getText());
               producer.send(new ProducerRecord<String, String>(
                  topicName, Integer.toString(j++), hashtage.getText()));
            }
         }
      }
      producer.close();
      Thread.sleep(5000);
      twitterStream.shutdown();
   }
}