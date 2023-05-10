package models.ergoplatform

import play.api.libs.json.Json
import play.api.libs.json.JsValue

final case class MOutputBox (
    id: String,
    txId: String,
    value: Long,
    index: Long,
    creationHeight: Long,
    ergoTree: String,
    address: String,
    assets: Seq[Token],
    additionalRegisters: Map[String, String]
);

object MOutputBox {
    implicit val json = Json.format[MOutputBox]
}
