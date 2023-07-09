package controllers

import javax.inject._

import play.api.Logging
import play.api.mvc._
import play.api.libs.json.Json

import model._
import database._
import util._

import scala.concurrent.ExecutionContext

@Singleton
class EventController @Inject() (
    protected val eventsDAO: EventsDAO,
    protected val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController
    with Logging {

  def getEvents(
      address: String,
      pluginName: Option[String]
  ): Action[AnyContent] = Action.async { implicit request =>
    if (pluginName.isEmpty) {
      eventsDAO
        .getEventsForAddress(address)
        .map(event => Ok(Json.toJson(event)));
    } else {
      eventsDAO
        .getEventsForAddressAndPluginName(address, pluginName.get)
        .map(event => Ok(Json.toJson(event)));
    }
  }

  def getPluginEvents(plugin: String): Action[AnyContent] = Action.async {
    eventsDAO
      .getEventsForPlugin(plugin)
      .map(event => Ok(Json.toJson(event)));
  }
}
