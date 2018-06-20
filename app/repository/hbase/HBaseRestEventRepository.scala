package repository.hbase

import models.{Event, TraceId, UserId}
import repository.EventRepository

import scala.concurrent.Future

class HBaseRestEventRepository extends EventRepository {
  def retrieveEventsForUserId(userId: UserId): Future[Either[String, Seq[Event]]] = ???
  def retrieveEventsForService(service: String): Future[Either[String, Seq[Event]]] = ???
  def retrieveEventByTraceId(traceId: TraceId): Future[Either[String, Option[Event]]] = ???
}
