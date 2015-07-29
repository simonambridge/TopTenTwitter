import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import TwitterHelper._

object TwitterPopularTags {
  def main(args: Array[String]) {
    
    // Checkpoint directory
    val checkpointDir = TwitterHelper.getCheckpointDirectory()

    // Configure Twitter credentials
    val apiKey = "llYiWOxG6REyKQ1sKB4pvEzeK"
    val apiSecret = "E2CTUmiJ8QsrIJ2xytzaTwfqxFJQIUJrRttT8xJ4MlQQ6dShCu"
    val accessToken = "780456391-LJdU3eRwf774porQHlxjpvBjFUpn304eQYlg3Fgo"
    val accessTokenSecret = "R3HXBfjgq4cN1xUgFlIdHZjJVYaZ5cvLrttR0q7uGWXcL"
    TwitterHelper.configureTwitterCredentials(apiKey, apiSecret, accessToken, accessTokenSecret)

    // Your code goes here
val ssc = new StreamingContext(new SparkConf(), Seconds(1))
val tweets = TwitterUtils.createStream(ssc, None)
val statuses = tweets.map(status => status.getText())

val words = statuses.flatMap(status => status.split(" "))
val hashtags = words.filter(word => word.startsWith("#"))
//hashtags.print()

val counts = hashtags.map(tag => (tag, 1)).reduceByKeyAndWindow(_ + _, _ - _, Seconds(60 * 5), Seconds(1))
//counts.print()

val sortedCounts = counts.map { case(tag, count) => (count, tag) }.transform(rdd => rdd.sortByKey(false))
sortedCounts.foreach(rdd => println("\nTop 10 hashtags:\n" + rdd.take(10).mkString("\n")))

ssc.checkpoint(checkpointDir)

ssc.start()
ssc.awaitTermination()

  }
}


