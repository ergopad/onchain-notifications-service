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
    "3Zo2un5B2c27hSexxAvAey6kTQbqLexakxFY2NVZKTQi2mW9o2rvG4ERDjiLtubK6dxLQzLYCWEQMcGx7P2h3PJn8Y7h2PP5vzC9g23trPsutouxBNCih4ZbhhFF5ECwGUTCCH6C7xuooky9nBa4Rwfb5mZbmtpZ6JyTQhfRsHhBVvYRwksfDpmv5rALGkcwSaQ3znAci3nEWmLt4rKGdpK3Ld76sfKUgYiLKEh1ggo81SYjz92fn8Nt7JsizcvTMAZ7qyqDtxF3Rmq58rYWSezV8qerosQQC3kPnzn7sUC7YmPzsYF1ajE2gGkvvQtk7GCxEjxTR6xTHp4j2zjnWCpfjkK2SGCkLUrEQnBQjHi7cME4bHtyHN4vBVptwDZQSo9tx1Ce3SwtB2DGVDE1hW3oUTXWfgayk7eoypHxBQuHQV8s9F2mwESfVkUEV2ZuDKhdnjabQRz89HctxPSh82kx5pZBrirxKPsuqWwRmqr2qboGGCZ57ySpbMhjqDbN8voGAmXD4A7sX3qWHmMmUHgvsk4TjheDicgXvuD1WMk8uT61nCEqR1q1eHxypovoj4YohADa4TZkt7mfcd2sYhEgksMsVaZP2iHr2foN8M32TWFWCeKr3qH69SKwUwC6DXPzDzq"
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
