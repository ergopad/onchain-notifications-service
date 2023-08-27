package processor.plugins

import java.util.UUID
import java.time.Instant

import javax.inject.Inject
import javax.inject.Singleton

import model.ergoplatform._
import model._

import play.api.libs.json.Json
import play.api.Logging

/** Plugin to Detect bPaideia Stake and Unstake Events
  */
@Singleton
class PaideiaStakingPlugin @Inject() ()
    extends EventProcessorPlugin
    with Logging {
  private val PAIDEIA_STAKE_STATE_TOKEN_ID =
    "00371ebfea98f3a9497404892ac43cd7ad45c6258d637af504b4cd6dd43d8afd"
  private val B_PAIDEIA_TOKEN_ID =
    "0040ae650c4ed77bcd20391493abe84c1a9bb58ee88e87f15670c801e2fc5983"
  private val STAKE_TRANSACTION_OUTPUT_LENGTH = 5
  private val USER_OUTPUT_INDEX = 2

  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean = {
    val outputBox = transaction.outputs.headOption
    if (outputBox.isEmpty) {
      return false
    }
    val token = outputBox.get.assets
      .find(token => token.tokenId == PAIDEIA_STAKE_STATE_TOKEN_ID)
    return token.isDefined
  }

  def isMatchingTransaction(transaction: Transaction): Boolean = {
    val outputBox = transaction.outputs.headOption
    if (outputBox.isEmpty) {
      return false
    }
    val token = outputBox.get.assets
      .find(token => token.tokenId == PAIDEIA_STAKE_STATE_TOKEN_ID)
    return token.isDefined
  }

  def processMempoolTransaction(transaction: MTransaction): Seq[Event] = {
    logger.info("Processing Paideia Staking MTransaction: " + transaction.id)
    if (transaction.outputs.length != STAKE_TRANSACTION_OUTPUT_LENGTH) {
      return Seq()
    }
    getStakeEvents(transaction)
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    logger.info("Processing Paideia Staking Transaction: " + transaction.id)
    if (transaction.outputs.length != STAKE_TRANSACTION_OUTPUT_LENGTH) {
      return Seq()
    }
    getStakeEvents(transaction)
  }

  private def getStakeEvents(transaction: MTransaction): Seq[Event] = {
    val userBox = transaction
      .outputs(USER_OUTPUT_INDEX)
    val outputTokens = userBox.assets
      .find(token => token.tokenId == B_PAIDEIA_TOKEN_ID)
    val isAddStake = outputTokens.isEmpty
    return Seq(
      generateEvent(
        transaction.id,
        if (isAddStake) "add_stake" else "remove_stake",
        userBox.address,
        "submitted"
      )
    )
  }

  private def getStakeEvents(transaction: Transaction): Seq[Event] = {
    val userBox = transaction
      .outputs(USER_OUTPUT_INDEX)
    val outputTokens = userBox.assets
      .find(token => token.tokenId == B_PAIDEIA_TOKEN_ID)
    val isAddStake = outputTokens.isEmpty
    return Seq(
      generateEvent(
        transaction.id,
        if (isAddStake) "add_stake" else "remove_stake",
        userBox.address,
        "confirmed"
      )
    )
  }

  private def generateEvent(
      transactionId: String,
      tpe: String,
      address: String,
      status: String
  ): Event = {
    Event(
      UUID.randomUUID,
      this.getClass.getSimpleName,
      transactionId,
      address,
      Json.toJson(Map("type" -> tpe, "status" -> status)),
      Instant.now
    )
  }
}
