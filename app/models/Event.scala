package models

import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json
import play.api.libs.json.JsValue

final case class Event(
    id: UUID,
    pluginName: String,
    transactionId: String,
    address: String,
    body: JsValue,
    timestamp: Instant
);

object Event {
  implicit val json = Json.format[Event];
}
