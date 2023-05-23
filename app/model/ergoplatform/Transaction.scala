package model.ergoplatform

import play.api.libs.json.Json

final case class Transaction (
    id: String,
    blockId: String,
    inclusionHeight: Int,
    timestamp: Long,
    index: Int,
    globalIndex: Int,
    numConfirmations: Int,
    inputs: Seq[InputBox],
    outputs: Seq[OutputBox],
    size: Int
)

object Transaction {
    implicit val json = Json.format[Transaction]
}
