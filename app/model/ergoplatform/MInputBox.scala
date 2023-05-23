package model.ergoplatform

import play.api.libs.json.Json
import play.api.libs.json.JsValue

final case class SpendingProof (
    proofBytes: Option[String],
    extension: Map[String, String]
)

object SpendingProof {
    implicit val json = Json.format[SpendingProof]
}

final case class MInputBox (
    id: String,
    transactionId: String,
    value: Long,
    index: Int,
    spendingProof: SpendingProof,
    outputTransactionId: String,
    outputIndex: Int,
    address: String
);

object MInputBox {
    implicit val json = Json.format[MInputBox]
}
