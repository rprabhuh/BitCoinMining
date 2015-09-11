import akka.actor._
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import java.security.MessageDigest


// Create the (immutable) base trait for messages
sealed trait BitcoinMessage

// Create the Messages
case class Start(numZeros: Int) extends BitcoinMessage
case class Mine(numZeros: Int) extends BitcoinMessage
case class Result(bitcoinStr: String) extends BitcoinMessage
case class Output(bitcoin: String, totalBitcoins: Int) extends BitcoinMessage

// Create the worker
class Worker(numZeros: Int) extends Actor {
  
  def receive = {
    case Mine(numZeros) => 
      val gatorid = "rprabhu"
      var flag = true
      var x = 0
      var y = 0
      while(flag) {
        x = 0
        y = 0
        var randomstring = scala.util.Random.alphanumeric.take(15).mkString
        val sha = MessageDigest.getInstance("SHA-256")
        val stringwithseed = gatorid + randomstring
        sha.update(stringwithseed.getBytes("UTF-8"))  
        val digest = sha.digest().map("%02X" format _).mkString

        // Check for the reqd number of leading zeros
        for(x <- 0 to numZeros - 1) {
          if(digest.charAt(x) == '0')
            y = y + 1
        }

        if(y >= numZeros) {
            flag = false
            sender ! Result(stringwithseed + "\t" + digest)
        }
      }

    case _ => println("INVALID MESSAGE")
              System.exit(1)
  }
}


// Create the master 
class Master(numWorkers: Int, numZeros: Int, listener: ActorRef) extends Actor {

  // Create the scheduling algorithm
  val workerRouter = context.actorOf(
  Props(new Worker(numZeros)).withRouter(RoundRobinRouter(numWorkers)), name = "workerRouter")

  def receive = {
      case Start(numZeros) =>
            for( i <- 0 until numWorkers) {
                workerRouter ! Mine(numZeros)
            }

      case Result(bitcoinStr) => 
          listener ! Output(bitcoinStr, numWorkers) 

      case _ => println("INVALID MESSAGE")
          System.exit(1)
  }
}


// Create the result listener
class Listener extends Actor {

  // Each worker will generate 1 bitcoin. And therefore, #bitcoins = #workers
  var workerFinished = 0

  def receive = {
    case Output(bitcoin, totalBitcoins) =>
      println("%s".format(bitcoin))
      workerFinished += 1
      if (workerFinished == totalBitcoins)
          System.exit(0)

    case _ => println("INVALID MESSAGE")
          System.exit(1)
  }
}


// Create the App
object BitcoinMining extends App {
  override def main(args: Array[String]) {

  if (isAllDigits(args(0)) == false) {
      println("INVALID ARGUMENT: Please specify a numeric argument.")
      System.exit(1)
  }

  // Get the command-line argument: Number of leading zeros in the hash
  val k = args(0).toInt 

  // Create an Akka system
  val system = ActorSystem("BitcoinMining")
  val numWorkers = 10

  // Create the listener
  val listener = system.actorOf(Props[Listener], name = "listener")
 
  // Create the Master
  val master = system.actorOf(Props(new Master(
      numWorkers, k, listener)),
      name = "master")

  // Send the master the start message
  master ! Start(k)

  }

  def isAllDigits(x: String) = x forall Character.isDigit
}
