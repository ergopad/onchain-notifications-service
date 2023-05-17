package processor

import javax.inject.Inject

import play.api.Logging

import models._
import models.ergoplatform._
import processor.plugins._

class EventProcessorCore @Inject() () extends Logging {
  private val EVENT_PROCESSOR_PLUGINS: Seq[EventProcessorPlugin] = Seq();

  def processConfirmedTransaction(transaction: Transaction) = {
    logger.info(
      transaction.id + ": " + TransactionStateStatus.CONFIRMED.toString
    )
  }

  def processPendingTransaction(transaction: MTransaction) = {
    logger.info(transaction.id + ": " + TransactionStateStatus.PENDING.toString)
  }

  def processFailedTransaction(transactionId: String) = {
    logger.info(transactionId + ": " + TransactionStateStatus.FAILED.toString)
  }
}
