package repository

import models.{ ErrorMessage, Event, TraceId, UserId }

import scala.concurrent.Future

trait EventRepository {
  def retrieveEventsForUserId(userId: UserId): Future[Either[ErrorMessage, Seq[Event]]]
  def retrieveEventsForService(service: String): Future[Either[ErrorMessage, Seq[Event]]]
  def retrieveEventByTraceId(traceId: TraceId): Future[Either[ErrorMessage, Option[Event]]]
}
