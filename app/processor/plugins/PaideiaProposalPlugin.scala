package processor.plugins

import java.util.UUID
import java.time.Instant

import javax.inject.Inject
import javax.inject.Singleton

import model.ergoplatform._
import model._

import play.api.libs.json.Json
import play.api.Logging

/** Plugin to Detect bPaideia Proposal Events
  */
@Singleton
class PaideiaProposalPlugin @Inject() ()
    extends EventProcessorPlugin
    with Logging {
  private val PROPOSAL_CONTRACT =
    "2ha1r5SsG1tLbsb1DTVQzSAv4MHzb1Zv9SYmXk9ZYVyGrotdKCpHPMSR3HEij11UnXhjX6gZ4AkgujFLxyEaw6J9hVjmYbCdTshHntKcFixaJZ71F1i5NnGe3aFUDVsC4i8uQgN1xqWhRZcSNDNq1NjTrVXMmvnQ5DJYtGfaatoa3Bzfoi8tKpHnCk6ggvBQGDLjPFTj8id2jbtN2vYKucekWWA5yzaLbuCnrWb1FeZP3E9evLfyfHNZwvUjtMJX7XhkYyBHW8H8ywEU4dx5Hr9WdA2mwejLuRZm15QXNYxMCqbGiaQFaz3FMQ6PFFiu9UyL9PMcynVxn"
  private val CREATE_PROPOSAL_OUTPUT_SIZE = 3
  private val PROPOSAL_CREATE_USER_ADDRESS_INDEX = 2

  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean = {
    if (transaction.outputs.length != CREATE_PROPOSAL_OUTPUT_SIZE) {
      return false;
    }
    val outputBox = transaction.outputs.head
    outputBox.address == PROPOSAL_CONTRACT
  }

  def isMatchingTransaction(transaction: Transaction): Boolean = {
    if (transaction.outputs.length != CREATE_PROPOSAL_OUTPUT_SIZE) {
      return false;
    }
    val outputBox = transaction.outputs.head
    outputBox.address == PROPOSAL_CONTRACT
  }

  def processMempoolTransaction(transaction: MTransaction): Seq[Event] = {
    logger.info("Processing Paideia Proposal MTransaction: " + transaction.id)
    val userAddress =
      transaction.outputs(PROPOSAL_CREATE_USER_ADDRESS_INDEX).address
    return Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(Map("type" -> "create_proposal", "status" -> "submitted")),
        Instant.now
      )
    )
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    logger.info("Processing Paideia Proposal Transaction: " + transaction.id)
    val userAddress =
      transaction.outputs(PROPOSAL_CREATE_USER_ADDRESS_INDEX).address
    return Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(Map("type" -> "create_proposal", "status" -> "confirmed")),
        Instant.now
      )
    )
  }
}
