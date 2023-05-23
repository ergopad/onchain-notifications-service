package model.ergoplatform

import play.api.libs.json.Json

final case class InputBox (
    boxId: String,
    value: Long,
    index: Int,
    spendingProof: Option[String],
    outputBlockId: String,
    outputTransactionId: String,
    outputIndex: Int,
    outputGlobalIndex: Int,
    outputCreatedAt: Int,
    outputSettledAt: Int,
    ergoTree: String,
    address: String,
    assets: Seq[Token],
)

object InputBox {
    implicit val json = Json.format[InputBox]
}
