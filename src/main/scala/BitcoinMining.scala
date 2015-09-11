import akka.actor._
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import java.security.MessageDigest


//Create the (immutable) base trait for messages
sealed trait BitcoinMessage

//Create the Messages
case class Start(numZeros: Int) extends BitcoinMessage
case class Mine(numZeros: Int) extends BitcoinMessage
case class Result(bitcoinStr: String) extends BitcoinMessage
case class Output(bitcoin: String) extends BitcoinMessage

//Create the worker
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
        val stringwithseed=gatorid+randomstring
        sha.update(stringwithseed.getBytes("UTF-8"))  
        val digest = sha.digest().map("%02X" format _).mkString; 
        for(x <- 0 to numZeros) {
          if(digest.charAt(x) == '0') {
            y = y +1
          }
        }
        if(y >= numZeros){
            flag = false
            sender ! Result(digest);
        }
      }
    case _ => println("INVALID")
  }

  // Mining Function

}


//Create the master 
class Master(numWorkers: Int, numMessages: Int, numZeros: Int, listener: ActorRef) extends Actor {

  //Initialize stuff
  
  
  //Create the scheduling algorithm
  val workerRouter = context.actorOf(
  Props(new Worker(numZeros)).withRouter(RoundRobinRouter(numWorkers)), name = "workerRouter")


  def receive = {
      case Start(numZeros) =>
            //println("Starting Workers");
            for( i <- 0 until numWorkers) {
                workerRouter ! Mine(numZeros)
            }

      case Result(bitcoinStr) => //println("Hashed value: %s".format(bitcoinStr));
                listener ! Output(bitcoinStr)

      case _ => println("INVALID")
  }



}

//Create the result listener
class Listener extends Actor {
  def receive = {
    case Output(bitcoin) =>
      println("Got result: %s".format(bitcoin))

    case _ => println("INVALID")
  }
}


//Create the App
object BitcoinMining extends App {
  override def main(args: Array[String]) {

  // Create an Akka system
  val system = ActorSystem("BitcoinMining")

  //Create the listener
  val listener = system.actorOf(Props[Listener], name = "listener")
 
  //Create the Master
  val master = system.actorOf(Props(new Master(
      20, 10, 4, listener)),
      name = "master")

  //Send the master the start message
  master ! Start(4)

  }
}
