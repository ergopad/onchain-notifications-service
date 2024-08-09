package model.paideia

import play.api.libs.json.Json
import play.api.libs.json.JsonConfiguration
import play.api.libs.json.JsonNaming.SnakeCase

final case class Dao(
    id: String,
    daoName: String,
    daoUrl: String
)

object Dao {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit val json = Json.format[Dao]
}
