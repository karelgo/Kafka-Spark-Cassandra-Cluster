name := "SparkWordCount"
version := "1.0"
scalaVersion := "2.11.12"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.4.3" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.4.3" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.4.3"
libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector_2.11" % "2.4.1"
libraryDependencies +=  "org.apache.spark" % "spark-sql_2.11" % "2.4.3" % "provided"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}