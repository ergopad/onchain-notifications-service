package model

import play.api.libs.json.Json

final case class DynamicConfig(
    key: String,
    config: String
);

object DynamicConfig {
  implicit val json = Json.format[DynamicConfig];
}
