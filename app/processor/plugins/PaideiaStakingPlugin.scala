package processor.plugins

import java.util.UUID
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.json.Json
import play.api.Logging

import database.DynamicConfigDAO
import model.ergoplatform._
import model.paideia._
import model._
import util.Util._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/** Plugin to Detect Paideia Stake and Unstake Events
  */
@Singleton
class PaideiaStakingPlugin @Inject() (
    protected val dynamicConfigDAO: DynamicConfigDAO
) extends EventProcessorPlugin
    with Logging {
  private val MIN_OUTPUT_LENGTH = 3
  private val CONTRACT_INDEX = 1
  private val USER_OUTPUT_INDEX = 2

  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean = {
    val outputs = transaction.outputs
    if (outputs.length < MIN_OUTPUT_LENGTH) {
      return false
    }
    val address = outputs(CONTRACT_INDEX).address
    findDaoConfig(address).isDefined
  }

  def isMatchingTransaction(transaction: Transaction): Boolean = {
    val outputs = transaction.outputs
    if (outputs.length < MIN_OUTPUT_LENGTH) {
      return false
    }
    val address = outputs(CONTRACT_INDEX).address
    findDaoConfig(address).isDefined
  }

  def processMempoolTransaction(transaction: MTransaction): Seq[Event] = {
    logger.info("Processing Paideia Staking MTransaction: " + transaction.id)
    if (transaction.outputs.length < MIN_OUTPUT_LENGTH) {
      return Seq()
    }
    getStakeEvents(transaction)
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    logger.info("Processing Paideia Staking Transaction: " + transaction.id)
    if (transaction.outputs.length < MIN_OUTPUT_LENGTH) {
      return Seq()
    }
    getStakeEvents(transaction)
  }

  private def getStakeEvents(transaction: MTransaction): Seq[Event] = {
    val userBox = transaction
      .outputs(USER_OUTPUT_INDEX)
    val contractBox = transaction.outputs(CONTRACT_INDEX)
    val daoConfig = findDaoConfig(contractBox.address).get
    Seq(
      generateEvent(
        transaction.id,
        daoConfig.url,
        "stake",
        userBox.address,
        "submitted"
      )
    )
  }

  private def getStakeEvents(transaction: Transaction): Seq[Event] = {
    val userBox = transaction
      .outputs(USER_OUTPUT_INDEX)
    val contractBox = transaction.outputs(CONTRACT_INDEX)
    val daoConfig = findDaoConfig(contractBox.address).get
    Seq(
      generateEvent(
        transaction.id,
        daoConfig.url,
        "stake",
        userBox.address,
        "confirmed"
      )
    )
  }

  private def generateEvent(
      transactionId: String,
      dao: String,
      tpe: String,
      address: String,
      status: String
  ): Event = {
    Event(
      UUID.randomUUID,
      this.getClass.getSimpleName,
      transactionId,
      address,
      Json.toJson(Map("dao" -> dao, "type" -> tpe, "status" -> status)),
      Instant.now
    )
  }

  private def getConfig: Seq[DaoConfig] = {
    val res =
      Await.result(dynamicConfigDAO.get("PaideiaConfigPlugin"), Duration.Inf)
    if (res.isEmpty) {
      return Seq()
    }
    val json = Json.parse(res.get.config)
    Json.fromJson[Seq[DaoConfig]](json).get
  }

  private def findDaoConfig(address: String): Option[DaoConfig] = {
    val config = getConfig
    val c = config.find(c =>
      parseHashFromPaideiaContractSignature(c.stake)
        .equals(pHashAddress(address)) ||
        parseHashFromPaideiaContractSignature(c.changeStake)
          .equals(pHashAddress(address)) ||
        parseHashFromPaideiaContractSignature(c.unstake).equals(
          pHashAddress(address)
        )
    )
    c
  }
}
