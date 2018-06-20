package consumers

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ ActorRef, ActorSystem }
import com.newmotion.akka.rabbitmq.{ ChannelActor, ConnectionActor, CreateChannel, _ }
import com.rabbitmq.client.{ ConnectionFactory, DefaultConsumer }
import com.typesafe.scalalogging.LazyLogging
import models.Event
import repository.EventRepository

class RabbitMQEventConsumer @Inject() (repository: EventRepository) extends LazyLogging {

  def setupSubscriber(): Unit = {
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
          Event.validate(bodyStr) match {
            case Some(e) => repository.insertEvent(e) map {
              case Left(err) => logger.error(s"Unable to insert event into database: ${err.msg}")
              case Right(_) => logger.info(s"Successfully inserted event with id [${e.traceId}] into database")
            }
            case None => logger.error("Error, unable to parse message into Event model.")
          }
        }
      }
      channel.basicConsume(queue, true, consumer)
    }
    connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))
  }

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
}
