package processor.plugins

import play.api.libs.json.Json
import play.api.Logging
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import java.util.UUID
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

import database.DynamicConfigDAO
import model.ergoplatform._
import model.paideia._
import model._
import util.Util._

/** Plugin to Detect Paideia Voting Events
  */
@Singleton
class PaideiaVotingPlugin @Inject() (
    protected val dynamicConfigDAO: DynamicConfigDAO
) extends EventProcessorPlugin
    with Logging {
  private val MIN_OUTPUT_LENGTH = 4
  private val CONTRACT_INDEX = 1
  private val USER_OUTPUT_INDEX = 3

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
    logger.info("Processing Paideia Voting MTransaction: " + transaction.id)
    val userAddress =
      transaction.outputs(USER_OUTPUT_INDEX).address
    val contractAddress = transaction.outputs(CONTRACT_INDEX).address
    val daoConfig = findDaoConfig(contractAddress).get
    Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(
          Map("dao" -> daoConfig.url, "type" -> "vote", "status" -> "submitted")
        ),
        Instant.now
      )
    )
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    logger.info("Processing Paideia Voting Transaction: " + transaction.id)
    val userAddress =
      transaction.outputs(USER_OUTPUT_INDEX).address
    val contractAddress = transaction.outputs(CONTRACT_INDEX).address
    val daoConfig = findDaoConfig(contractAddress).get
    return Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(
          Map("dao" -> daoConfig.url, "type" -> "vote", "status" -> "confirmed")
        ),
        Instant.now
      )
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
      parseHashFromPaideiaContractSignature(c.vote)
        .equals(pHashAddress(address))
    )
    c
  }
}
