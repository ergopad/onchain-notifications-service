package models.ergoplatform

import play.api.libs.json.Json

final case class MTransaction (
    id: String,
    inputs: Seq[MInputBox],
    outputs: Seq[MOutputBox]
)

object MTransaction {
    implicit val json = Json.format[MTransaction]
}
