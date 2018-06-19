package models

import play.api.libs.json.{ JsObject, JsValue, Json, Writes }

import scala.util.Random

case class Event(userId: Int, service: String, uuid: String, eventType: String) {
  override def toString: String = s"$userId, $service, $uuid, $eventType"
}

object Event {

  private val services: List[String] = List("bi", "sbr", "ai")
  private val eventTypes: List[String] = List("search", "edit")

  implicit val eventWrites = new Writes[Event] {
    override def writes(e: Event): JsValue = {
      import e._
      JsObject(Seq(
        "userId" -> Json.toJson(userId),
        "service" -> Json.toJson(service),
        "uuid" -> Json.toJson(uuid),
        "eventType" -> Json.toJson(eventType)
      ))
    }
  }

  def apply(s: String): Event = {
    val split = s.trim.split(",").toList
    Event(split(0).toInt, split(1), split(2), split(3))
  }

  def randomEvent: Event = {
    val r = scala.util.Random
    Event(r.nextInt(10000), Random.shuffle(services).head, java.util.UUID.randomUUID.toString, Random.shuffle(eventTypes).head)
  }
}
