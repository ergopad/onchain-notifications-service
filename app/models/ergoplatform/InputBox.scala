package models.ergoplatform

import play.api.libs.json.Json

final case class InputBox (
    boxId: String,
    value: Long,
    index: Int,
    spendingProof: String,
    outputBlockId: String,
    outputTransactionId: String,
    outputIndex: Int,
    outputGlobalIndex: Int,
    outputCreatedAt: Int,
    outputSettledAt: Int,
    ergoTree: String,
    address: String,
    assets: Seq[Token],
    additionalRegisters: Map[String, String]
)

object InputBox {
    implicit val json = Json.format[InputBox]
}
