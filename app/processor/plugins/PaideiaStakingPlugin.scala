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
    "26f11cf7a5fa7faea37341ce8e0a1de0b82e100b6835707504ee0575996aa8dd"
  private val B_PAIDEIA_TOKEN_ID =
    "f60fb5aa6127d4a2b537a91518a15eab1d21099cd34bc2e4c9f59022c3dd5af2"
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
