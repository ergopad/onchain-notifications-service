package processor.plugins

import java.util.UUID
import java.time.Instant

import javax.inject.Inject
import javax.inject.Singleton

import model.ergoplatform._
import model._

import play.api.libs.json.Json
import play.api.Logging

/** Plugin to Detect bPaideia Voting Events
  */
@Singleton
class PaideiaVotingPlugin @Inject() ()
    extends EventProcessorPlugin
    with Logging {
  private val PROPOSAL_VOTING_CONTRACT =
    "9Uw9EcVR9bTZiBnKeqMaR3kRuEpe18BPe7a3WrT9Ht9ugLJzKweht4zSZTgeoXehqrUpjRgHj2vELNGruCbwniWHhgj3FpJzf8ReMD9czG8rpezXFw6ZD5xv269N4z2AS4uGhEW2JVgCFPPfAn3CfoR9fHVHM7pTewwRHtkQTexXk7uPc7eH7QsV4vVYrrPxRdBGyLPuVZf26qJQGgi5Fe3NpvJPRbue1tXUzSQGUT4YvHVvd8iSZmP7PmSrAG1tuUuVVeDZdpFgnxS8id7PWsfXipphs4hhL6aJ1"
  private val VOTE_OUTPUT_SIZE = 3
  private val VOTING_USER_ADDRESS_INDEX = 2

  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean = {
    if (transaction.outputs.length != VOTE_OUTPUT_SIZE) {
      return false;
    }
    val outputBox = transaction.outputs.head
    outputBox.address == PROPOSAL_VOTING_CONTRACT
  }

  def isMatchingTransaction(transaction: Transaction): Boolean = {
    if (transaction.outputs.length != VOTE_OUTPUT_SIZE) {
      return false;
    }
    val outputBox = transaction.outputs.head
    outputBox.address == PROPOSAL_VOTING_CONTRACT
  }

  def processMempoolTransaction(transaction: MTransaction): Seq[Event] = {
    logger.info("Processing Paideia Voting MTransaction: " + transaction.id)
    val userAddress =
      transaction.outputs(VOTING_USER_ADDRESS_INDEX).address
    return Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(Map("type" -> "vote", "status" -> "submitted")),
        Instant.now
      )
    )
  }

  def processTransaction(transaction: Transaction): Seq[Event] = {
    logger.info("Processing Paideia Voting Transaction: " + transaction.id)
    val userAddress =
      transaction.outputs(VOTING_USER_ADDRESS_INDEX).address
    return Seq(
      Event(
        UUID.randomUUID,
        this.getClass.getSimpleName,
        transaction.id,
        userAddress,
        Json.toJson(Map("type" -> "vote", "status" -> "confirmed")),
        Instant.now
      )
    )
  }
}
