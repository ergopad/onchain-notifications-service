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
  private val DUPLICATE_EVENT_TIMESTAMP_DELTA_SECONDS = 600;

  def processConfirmedTransaction(transaction: Transaction) = {
    logger.info(
      transaction.id + ": " + TransactionStateStatus.CONFIRMED.toString
    )
    plugins.asScala.foreach(plugin => {
      try {
        val shouldProcess = plugin.isMatchingTransaction(transaction)
        if (shouldProcess) {
          val events = plugin.processTransaction(transaction)
          publishEvents(events)
        }
      } catch {
        case e: Exception => logger.error(e.getMessage(), e)
      }
    })
  }

  def processPendingTransaction(transaction: MTransaction) = {
    logger.info(transaction.id + ": " + TransactionStateStatus.PENDING.toString)
    plugins.asScala.foreach(plugin => {
      try {
        val shouldProcess = plugin.isMatchingMempoolTransaction(transaction)
        if (shouldProcess) {
          val events = plugin.processMempoolTransaction(transaction)
          val dedupedEvents = events.filter(event => verifyDuplicates(event))
          publishEvents(dedupedEvents)
        }
      } catch {
        case e: Exception => logger.error(e.getMessage(), e)
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

  private def verifyDuplicates(event: Event): Boolean = {
    val address = event.address
    val pluginName = event.pluginName
    val events = Await.result(
      eventsDAO.getEventsForAddressAndPluginName(address, pluginName),
      Duration.Inf
    )
    val matchingEvents = events.filter(e => {
      getEventHash(e) == getEventHash(event) && isMatchingTimestamp(e, event)
    })
    matchingEvents.headOption.isEmpty
  }

  private def getEventHash(event: Event): Int = {
    val text = event.address + event.pluginName + event.body.toString
    text.hashCode
  }

  private def isMatchingTimestamp(e1: Event, e2: Event): Boolean = {
    (e1.timestamp.getEpochSecond - e2.timestamp.getEpochSecond).abs <= DUPLICATE_EVENT_TIMESTAMP_DELTA_SECONDS
  }
}
