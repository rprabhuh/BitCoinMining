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
case class Result(bitcoinStr: String) extends BitcoinMessage
case class Output(bitcoin: String, totalBitcoins: Int) extends BitcoinMessage
case class RequestWork() extends BitcoinMessage
case class Verify(randomString: String, numZeros: Int) extends BitcoinMessage
case class SHAResult(found: Boolean, bcString: String) extends BitcoinMessage

//Create SHA Worker
class SHAWorker() extends Actor {
  def receive ={
    case Verify(randomstring, numZeros) =>
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
      sender ! SHAResult(found, randomstring + "\t" + digest)
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
  def receive = {
    case Start(ip) =>
      masterRef = "akka.tcp://BitcoinMining@" + ip + ":3000/user/MasterActor"
      master = context.actorFor(masterRef)
      master ! RequestWork
    case Mine(num) => 
      numZeros = num
      randomstring = scala.util.Random.alphanumeric.take(15).mkString

      //Create a workerRouter
      val SHAworkerRouter = context.actorOf(
        Props[SHAWorker].withRouter(RoundRobinRouter(numshaWorkers)), name = "shaworkerRouter")

      //Activate the workers 
      for( i <- 0 until numshaWorkers) {
        SHAworkerRouter ! Verify(gatorid+randomstring, numZeros)
      }

    case SHAResult(found, sha) =>
      if(repliedtoMaster) {
        context.stop(sender);
      } else {
        if(found) {
          repliedtoMaster = true
          master ! Result(sha)
          context.stop(sender);
        } else {
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

  // Create the scheduling algorithm

  def receive = {
    case RequestWork =>
      sender ! Mine(numZeros)
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
//      if (workerFinished == totalBitcoins) {
        //System.exit(0)
 //     }

    case _ => println("INVALID MESSAGE")
    System.exit(1)
  }
}


// Create the App
object BitcoinMining extends App {
  override def main(args: Array[String]) {
    var ip :String = ""
    var k = 0

    //Validate Input
    if(args.length != 1) {
      println("ERROR: Usage >run <num_leading_zeros> or\n >run <ip_address>") 
      System.exit(1)
    }

    var configFile:String = ""
    // When running as a client
    if (isAllDigits(args(0)) == false) {
      ip = args(0)
      configFile = getClass.getClassLoader.getResource("client_application.conf").getFile
    } 
    // When running as a server
    else {
    // Get the command-line argument: Number of leading zeros in the hash
      k = args(0).toInt 
      configFile = getClass.getClassLoader.getResource("server_application.conf").getFile
      ip = ConfigFactory.load("server_application").getString("akka.remote.netty.tcp.hostname")
    }

    // Create an Akka system
    //val config = ConfigFactory.parseFile(new File(configFile))
    println("IP: " + ip)
    val config = ConfigFactory.parseFile(new File(configFile))
    val system = ActorSystem("BitcoinMining", config)
    val numWorkers = 10 

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
