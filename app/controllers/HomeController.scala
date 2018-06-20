package controllers

import akka.actor.{ ActorRef, ActorSystem }
import org.joda.time.DateTime
import com.newmotion.akka.rabbitmq._
import models.Event
import play.api.libs.json.Json

import play.api.mvc.{ Controller, _ }

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

    def setupSubscriber(channel: Channel, self: ActorRef) {
      val queue = channel.queueDeclare().getQueue
      channel.queueBind(queue, exchange, "")
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
          val bodyStr = fromBytes(body)
          println(s"got b from sub: $bodyStr")
          Event.validate(bodyStr) match {
            case Some(e) => events = e :: events
            case None => println("Error, unable to parse message into Event model.")
          }
        }
      }
      channel.basicConsume(queue, true, consumer)
    }
    connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))
  }

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")

  def getEvents = Action {
    Ok(Json.toJson(events))
  }

  def health = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"{Status: Ok, Uptime: ${uptimeInMillis}ms, Date and Time: " + new DateTime(startTime) + "}")
  }

  /*
   * preflight is used for local OPTIONS requests that precede PUT/DELETE requests. An empty Ok() response allows
   * the actual PUT/DELETE request to be sent.
   */
  def preflight(all: String) = Action {
    Ok("")
  }
}
