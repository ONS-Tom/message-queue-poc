package controllers.v1

import javax.inject.Inject

import controllers.v1.api.EventApi
import models._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import play.mvc.Controller
import repository.EventRepository

class EventController @Inject() (repository: EventRepository) extends Controller with EventApi {

  def retrieveEventsForService(service: String): Action[AnyContent] = Action.async {
    repository.retrieveEventsForService(service) map { errorOrEvents =>
      errorOrEvents.fold(resultOnFailure, resultsOnSuccess)
    }
  }

  def retrieveEventsForUserId(userId: String): Action[AnyContent] = Action.async {
    repository.retrieveEventsForUserId(UserId(userId)) map { errorOrEvents =>
      errorOrEvents.fold(resultOnFailure, resultsOnSuccess)
    }
  }

  def retrieveEventByTraceId(traceId: String): Action[AnyContent] = Action.async {
    repository.retrieveEventByTraceId(TraceId(traceId)) map { errorOrMaybeEvent =>
      errorOrMaybeEvent.fold(resultOnFailure, resultOnSuccess)
    }
  }

  private def resultOnFailure(errorMessage: ErrorMessage): Result = errorMessage match {
    case g: GatewayTimeout => Results.GatewayTimeout
    case s: ServiceUnavailable => Results.ServiceUnavailable
    case _ => Results.InternalServerError
  }

  private def resultsOnSuccess(events: Seq[Event]): Result =
    if (events.isEmpty) NotFound
    else Ok(Json.toJson(events))

  private def resultOnSuccess(maybeEvent: Option[Event]): Result =
    maybeEvent.fold[Result](NotFound)(event => Ok(Json.toJson(event)))
}
