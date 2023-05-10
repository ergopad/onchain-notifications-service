package models.ergoplatform

import play.api.libs.json.Json

final case class OutputBox (
    boxId: String,
    transactionId: String,
    blockId: String,
    value: Long,
    index: Int,
    globalIndex: Int,
    creationHeight: Int,
    settlementHeight: Int,
    ergoTree: String,
    address: String,
    assets: Seq[Token],
    additionalRegisters: Map[String, String],
    spentTransactionId: Option[String],
    mainChain: Boolean
)

object OutputBox {
    implicit val json = Json.format[OutputBox]
}
