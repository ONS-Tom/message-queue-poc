package controllers

import org.joda.time.DateTime

import play.api.mvc.{ Controller, _ }

class HomeController extends Controller {
  private[this] val startTime = System.currentTimeMillis()

  def health = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"{Status: Ok, Uptime: ${uptimeInMillis}ms, Date and Time: " + new DateTime(startTime) + "}")
  }
}
