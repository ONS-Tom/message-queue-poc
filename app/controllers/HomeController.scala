package controllers

import akka.actor.{ ActorRef, ActorSystem }
import org.joda.time.DateTime
import com.newmotion.akka.rabbitmq._
import models.Event
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.{ Controller, _ }

import scala.concurrent.Future

class HomeController extends Controller {
  private[this] val startTime = System.currentTimeMillis()

  // The below var is temporary
  private var events: List[Event] = List()

  init()

  def init(): Unit = {
    implicit val system = ActorSystem()
    val factory = new ConnectionFactory()
    val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
    val exchange = "amq.fanout"

    def setupPublisher(channel: Channel, self: ActorRef) {
      val queue = channel.queueDeclare().getQueue
      channel.queueBind(queue, exchange, "")
    }
    connection ! CreateChannel(ChannelActor.props(setupPublisher), Some("publisher"))

    def setupSubscriber(channel: Channel, self: ActorRef) {
      val queue = channel.queueDeclare().getQueue
      channel.queueBind(queue, exchange, "")
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
          val b = fromBytes(body)
          events = Event.apply(b) :: events
          println("received: " + fromBytes(body))
        }
      }
      channel.basicConsume(queue, true, consumer)
    }
    connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))

    Future {
      def loop(n: Long) {
        val publisher = system.actorSelection("/user/rabbitmq/publisher")

        def publish(channel: Channel) {
          channel.basicPublish(exchange, "", null, toBytes(Event.randomEvent.toString))
        }
        publisher ! ChannelMessage(publish, dropIfNoChannel = false)

        Thread.sleep(1000)
        loop(n + 1)
      }
      loop(0)
    }
  }

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def toBytes(x: Any) = x.toString.getBytes("UTF-8")

  def getEvents = Action {
    Ok(Json.toJson(events))
  }

  def health = Action {
    val uptimeInMillis = uptime()
    Ok(s"{Status: Ok, Uptime: ${uptimeInMillis}ms, Date and Time: " + new DateTime(startTime) + "}")
  }

  private def uptime(): Long = {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    uptimeInMillis
  }

  /*
   * preflight is used for local OPTIONS requests that precede PUT/DELETE requests. An empty Ok() response allows
   * the actual PUT/DELETE request to be sent.
   */
  def preflight(all: String) = Action {
    Ok("")
  }
}
