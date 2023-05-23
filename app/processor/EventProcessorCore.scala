package processor

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._

import database._
import model._
import model.ergoplatform._
import processor.plugins._

@Singleton
class EventProcessorCore @Inject() (
    protected val plugins: java.util.Set[EventProcessorPlugin],
    protected val eventsDAO: EventsDAO
) extends Logging {
  def processConfirmedTransaction(transaction: Transaction) = {
    logger.info(
      transaction.id + ": " + TransactionStateStatus.CONFIRMED.toString
    )
    plugins.asScala.foreach(plugin => {
      val shouldProcess = plugin.isMatchingTransaction(transaction)
      if (shouldProcess) {
        val events = plugin.processTransaction(transaction)
        publishEvents(events)
      }
    })
  }

  def processPendingTransaction(transaction: MTransaction) = {
    logger.info(transaction.id + ": " + TransactionStateStatus.PENDING.toString)
    plugins.asScala.foreach(plugin => {
      val shouldProcess = plugin.isMatchingMempoolTransaction(transaction)
      if (shouldProcess) {
        val events = plugin.processMempoolTransaction(transaction)
        publishEvents(events)
      }
    })
  }

  def processFailedTransaction(transactionId: String) = {
    logger.warn(transactionId + ": " + TransactionStateStatus.FAILED.toString)
  }

  private def publishEvents(events: Seq[Event]) = {
    events.foreach(event => {
      Await.result(eventsDAO.createEvent(event), Duration.Inf)
    })
  }
}
