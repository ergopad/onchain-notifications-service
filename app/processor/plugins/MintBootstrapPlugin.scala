package processor.plugins

import java.util.UUID
import java.time.Instant

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json.Json

import model._
import model.ergoplatform._

/** Sample Implementation
  */
@Singleton
class MintBootstrapPlugin @Inject() () extends EventProcessorPlugin {
  private val CONTRACT_PREFIX = "MZ3Xgdn"

  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean = {
    transaction.outputs.length > 0 &&
    transaction.outputs(0).address.startsWith(CONTRACT_PREFIX)
  }

  def isMatchingTransaction(transaction: Transaction): Boolean = {
    transaction.outputs.length > 0 &&
    transaction.outputs(0).address.startsWith(CONTRACT_PREFIX)
  }

  def processMempoolTransaction(transaction: MTransaction): Seq[Event] = {
    Seq(
      generateEvent(transaction.id, transaction.inputs(0).address, "submitted")
    )
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    Seq(
      generateEvent(transaction.id, transaction.inputs(0).address, "confirmed")
    )
  }

  private def generateEvent(
      transactionId: String,
      address: String,
      status: String
  ): Event = {
    Event(
      UUID.randomUUID,
      this.getClass.getSimpleName,
      transactionId,
      address,
      Json.toJson(Map("type" -> "MintTransaction", "status" -> status)),
      Instant.now
    )
  }
}
