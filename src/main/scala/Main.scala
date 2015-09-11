import akka.actor._
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration

//Create the base trait for messages
sealed trait bitCoin

//Create the Messages
class start extends bitCoin


//Create the worker
class Worker extends Actor{
  def receive = {
    /*
     * case <messagetype> => some action
     */
    case start =>
      println("Started Code");
  }

}

//Create the master 
class Master(numWorkers: Int, numMessages: Int, nrOfElements: Int, numZeros: Int, listener: ActorRef) extends Actor {

  //Initialize stuff
  
  
  //Create the scheduling algorithm
  val workerRouter = context.actorOf(
  Props[Worker].withRouter(RoundRobinRouter(numWorkers)), name = "workerRouter")


  def receive = {
    /*
     * case <messagetype> => some action
     *
     * case Result =>
     * Do a bunch of things and send a message to the listener
     */
    case start =>
      println("Started Code");
  }


}

//Create the result listener
class Listener extends Actor {
  def receive = {
    case start =>
      println("Got result");
  }
}


//Create the App
object Main extends App {
  //Create the listener
 
  
  //Create the Master
  
  
  //Send the master the start message
  
}
