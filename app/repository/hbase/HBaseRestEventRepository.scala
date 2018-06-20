package repository.hbase

import models.{ ErrorMessage, Event, TraceId, UserId }
import repository.EventRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class HBaseRestEventRepository extends EventRepository {

  private var events: Seq[Event] = Seq()

  def retrieveEventsForUserId(userId: UserId): Future[Either[ErrorMessage, Seq[Event]]] = {
    println(s"events: ${events}")
    Future(Right(events.filter(_.userId == userId.value)))
  }

  def retrieveEventsForService(service: String): Future[Either[ErrorMessage, Seq[Event]]] =
    Future(Right(events.filter(_.service == service)))

  def retrieveEventByTraceId(traceId: TraceId): Future[Either[ErrorMessage, Option[Event]]] =
    Future(Right(events.find(_.traceId == traceId.value)))

  def insertEvent(event: Event): Future[Either[ErrorMessage, Unit]] = Future {
    events = events :+ event
    Right()
  }
}
