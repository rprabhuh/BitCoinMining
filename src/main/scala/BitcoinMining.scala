import akka.actor._
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import com.typesafe.config.ConfigFactory
import java.security.MessageDigest
import java.io.File


// Create the (immutable) base trait for messages
sealed trait BitcoinMessage

// Create the Messages
case class Start(ip: String) extends BitcoinMessage
case class Mine(numZeros: Int) extends BitcoinMessage
case class Result(bitcoinStr: String, totalStrings: Int, timeTaken: Long) extends BitcoinMessage
case class Output(bitcoin: String, totalBitcoins: Int, totalStrings: Int, timeTaken: Long, timeForWorkers: Long) extends BitcoinMessage
case class RequestWork() extends BitcoinMessage
case class Verify(randomString: String, numZeros: Int) extends BitcoinMessage
case class SHAResult(found: Boolean, bcString: String, timeTaken: Long) extends BitcoinMessage


//Create SHA Worker
class SHAWorker() extends Actor {

  def receive ={
    case Verify(randomstring, numZeros) =>
      var startTime = System.currentTimeMillis()
      var x = 0
      var y = 0
      val sha = MessageDigest.getInstance("SHA-256")
      sha.update(randomstring.getBytes("UTF-8"))  
      val digest = sha.digest().map("%02X" format _).mkString
      var found = false

      // Check for the required number of leading zeros
      for(x <- 0 to numZeros - 1) {
        if(digest.charAt(x) == '0')
          y = y + 1
      }

      if(y >= numZeros) {
        found = true 
      }
      var totaltime = System.currentTimeMillis() - startTime
      sender ! SHAResult(found, randomstring + "\t" + digest, totaltime)
    case _ =>
      println("Error")
  }
}


// Create the worker
class Worker() extends Actor {
  var masterRef = ""
  var master:ActorRef = null
  val gatorid = "rprabhu"
  var randomstring = ""
  val numshaWorkers = 10 
  var numZeros = 0
  var repliedtoMaster = false
  var startTime = 0L
  var stringsGenerated = 0
  var timeSHA = 0L

  def receive = {
    case Start(ip) =>
      if (startTime == 0)
      		startTime = System.currentTimeMillis()
      masterRef = "akka.tcp://BitcoinMining@" + ip + ":3000/user/MasterActor"
      master = context.actorFor(masterRef)
      master ! RequestWork

    case Mine(num) => 

      numZeros = num

      //Create a workerRouter
      val SHAworkerRouter = context.actorOf(
        Props[SHAWorker].withRouter(RoundRobinRouter(numshaWorkers)), name = "shaworkerRouter")

      //Activate the workers 
      for( i <- 0 until numshaWorkers) {
        randomstring = scala.util.Random.alphanumeric.take(15).mkString
        SHAworkerRouter ! Verify(gatorid+randomstring, numZeros)
      }

    case SHAResult(found, sha, timeTaken) =>
      timeSHA += timeTaken

      if(repliedtoMaster) {
        context.stop(sender);
      } else {
        if(found) {
          repliedtoMaster = true
          master ! Result(sha, stringsGenerated + numshaWorkers, System.currentTimeMillis()-startTime)
          context.stop(sender);
        } else {
          stringsGenerated += 1
          randomstring = scala.util.Random.alphanumeric.take(15).mkString
          sender ! Verify(gatorid+randomstring, numZeros)
        }
      }

    case _ => println("INVALID MESSAGE")
    	System.exit(1)
  }
}


// Create the master 
class Master(numWorkers: Int, numZeros: Int, listener: ActorRef) extends Actor {

  var startTime = 0L
  var timeForWorkers = 0L
  var totalStrings = 0
  
  def receive = {
    case RequestWork =>
      if(startTime == 0)
        startTime = System.currentTimeMillis()
      sender ! Mine(numZeros)

    case Result(bitcoinStr, stringsGenerated, timeTaken) => 
      timeForWorkers += timeTaken
      totalStrings += stringsGenerated
      listener ! Output(bitcoinStr, numWorkers, totalStrings, System.currentTimeMillis() - startTime, timeForWorkers) 

    case _ => println("INVALID MESSAGE")
    	System.exit(1)
  }
}


// Create the result listener
class Listener extends Actor {

  // Each worker will generate 1 bitcoin. And therefore, #bitcoins = #workers
  var workerFinished = 0
  var realtime = 0L
  var parallelism = 0F

  def receive = {
    case Output(bitcoin, totalBitcoins, totalStrings, timeTaken, timeForWorkers) =>
      println("%s".format(bitcoin))
      workerFinished += 1
      realtime += timeTaken
      if (workerFinished == totalBitcoins) {
      	println("Work Units: " + totalStrings)
        println("Real time = " + timeTaken + "\tCPU Time = " + timeForWorkers)
        parallelism = timeForWorkers/timeTaken
        println("Parallelism: " + parallelism)
      }

    case _ => println("INVALID MESSAGE")
    	System.exit(1)
  }
}


// Create the App
object BitcoinMining extends App {
  override def main(args: Array[String]) {

  	//var progStartTime = System.currentTimeMillis()

    var ip:String = ""
    var k = 0

    //Validate Input
    if(args.length != 1) {
      println("ERROR: To run as server >run <num_leading_zeros> \n OR \n To run as client >run <ip_address>") 
      System.exit(1)
    }

    // When running as a client
    if (isAllDigits(args(0)) == false) {
      ip = args(0)
    } 
    // When running as a server/Master
    else {
      // Get the command-line argument: Number of leading zeros in the hash
      k = args(0).toInt 
    }

    // Create an Akka system
    val system = ActorSystem("BitcoinMining")
    val numWorkers = 10 
    if (ip == "")
        ip = ConfigFactory.load().getString("akka.remote.netty.tcp.hostname")

    //Create a workerRouter
    val workerRouter = system.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(numWorkers)), name = "workerRouter")

    // Create the listener
    val listener = system.actorOf(Props[Listener], name = "listener")

    // Create the Master
    val master = system.actorOf(Props(new Master(
        numWorkers, k, listener)),
  		name = "MasterActor")

    //Activate the workers 
    for( i <- 0 until numWorkers) {
      workerRouter ! Start(ip)
    }
  }

  def isAllDigits(x: String) = x forall Character.isDigit
}
