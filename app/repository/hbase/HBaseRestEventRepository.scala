package repository.hbase

import models.{ ErrorMessage, Event, TraceId, UserId }
import repository.EventRepository

import scala.concurrent.Future

class HBaseRestEventRepository extends EventRepository {
  def retrieveEventsForUserId(userId: UserId): Future[Either[ErrorMessage, Seq[Event]]] = ???
  def retrieveEventsForService(service: String): Future[Either[ErrorMessage, Seq[Event]]] = ???
  def retrieveEventByTraceId(traceId: TraceId): Future[Either[ErrorMessage, Option[Event]]] = ???
}
