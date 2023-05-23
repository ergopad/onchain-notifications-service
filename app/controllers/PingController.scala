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
class PingController @Inject() (
    protected val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController
    with Logging {

  def ping(): Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(PingResponse(status = "ok", message = "Hello World!")))
  }
}
