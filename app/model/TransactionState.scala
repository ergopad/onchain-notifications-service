package model

import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes

import slick.jdbc.PostgresProfile.api._

import java.util.UUID

object TransactionStateStatus extends Enumeration {
  type TransactionStateStatus = Value
  val SUBMITTED, PENDING, CONFIRMED, FAILED = Value

  implicit val readsTransactionStateStatus =
    Reads.enumNameReads(TransactionStateStatus)
  implicit val writesTransacionStateStatus = Writes.enumNameWrites
  implicit val statusMapper =
    MappedColumnType.base[TransactionStateStatus, String](
      e => e.toString,
      s => TransactionStateStatus.withName(s)
    )
}

final case class TransactionState(
    id: UUID,
    transactionId: String,
    status: TransactionStateStatus.Value,
    retryCount: Int
)

object TransactionState {
  implicit val json = Json.format[TransactionState]
}
