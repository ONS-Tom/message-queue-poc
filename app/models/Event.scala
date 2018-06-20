package models

import play.api.libs.json.{ JsObject, JsValue, Json, Writes }

import scala.util.Try

case class Event(userId: String, service: String, traceId: String, eventType: String, timeStamp: Int) {
  override def toString: String = s"$userId, $service, $traceId, $eventType"
}

object Event {

  implicit val eventWrites = new Writes[Event] {
    override def writes(e: Event): JsValue = {
      import e._
      JsObject(Seq(
        "userId" -> Json.toJson(userId),
        "service" -> Json.toJson(service),
        "traceId" -> Json.toJson(traceId),
        "eventType" -> Json.toJson(eventType),
        "timeStamp" -> Json.toJson(timeStamp)
      ))
    }
  }

  def validate(s: String): Option[Event] = {
    val split = s.trim.split(",").toList.map(_.trim)
    Try(Event(split(0), split(1), split(2), split(3), split(4).toInt)).toOption
  }
}
