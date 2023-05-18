package tasks

import akka.actor.ActorSystem

import java.util.UUID

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import slick.basic.DatabasePublisher
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import database._
import models._
import models.ergoplatform._
import processor._
import util._

@Singleton
class TransactionUpdaterTask @Inject() (
    protected val ergoNodeClient: ErgoNodeClient,
    protected val transactionsDAO: TransactionsDAO,
    protected val eventProcessor: EventProcessorCore,
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val actorSystem: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {
  private val MAX_RETRY_COUNT = 3;

  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 5.seconds,
    delay = 60.seconds
  )(() =>
    try {
      val pendingTransactionStates =
        Await.result(transactionsDAO.getPendingTransactions, Duration.Inf)
      val confirmedTransactions =
        getConfirmedTransactions(pendingTransactionStates)
      val confirmedTransactionsMap =
        getConfirmedTransactionsMap(confirmedTransactions)
      val pendingTransactions =
        getUnconfirmedTransactions(pendingTransactionStates)
      val updateMap =
        buildStateUpdateMap(confirmedTransactions, pendingTransactions)
      val updatedTransactionStates =
        pendingTransactionStates.map(transactionState => {
          val update = updateMap.get(transactionState.transactionId)
          if (update.isDefined) {
            getTransactionUpdatedStateIfRequired(transactionState, update.get)
          } else {
            getTransactionStateForRetry(transactionState)
          }
        })
      updatedTransactionStates.foreach(transactionState => {
        Await.result(
          transactionsDAO.updateTransactionState(transactionState),
          Duration.Inf
        )
        notifyEventsSystem(
          transactionState.transactionId,
          confirmedTransactionsMap.get(transactionState.transactionId),
          transactionState.status
        );
      })
    } catch {
      case e: Exception => logger.error(e.getMessage(), e)
    }
  )

  private def getConfirmedTransactions(
      transactionStates: Seq[TransactionState]
  ): Seq[Option[Transaction]] = {
    transactionStates.map(transactionState =>
      ergoNodeClient.getTransaction(transactionState.transactionId)
    )
  }

  private def getConfirmedTransactionsMap(
      transactions: Seq[Option[Transaction]]
  ): Map[String, Transaction] = {
    val transactionsMap =
      scala.collection.mutable.Map[String, Transaction]()
    transactions.foreach(transaction => {
      if (transaction.isDefined) {
        transactionsMap.put(transaction.get.id, transaction.get)
      }
    })
    transactionsMap.toMap
  }

  private def getUnconfirmedTransactions(
      transactionStates: Seq[TransactionState]
  ): Seq[Option[MTransaction]] = {
    transactionStates.map(transactionState =>
      ergoNodeClient.getUnconfirmedTransaction(transactionState.transactionId)
    )
  }

  private def buildStateUpdateMap(
      confirmed: Seq[Option[Transaction]],
      pending: Seq[Option[MTransaction]]
  ): Map[String, TransactionStateStatus.Value] = {
    val stateUpdateMap =
      scala.collection.mutable.Map[String, TransactionStateStatus.Value]()
    confirmed.foreach(transaction => {
      if (transaction.isDefined) {
        stateUpdateMap.put(transaction.get.id, TransactionStateStatus.CONFIRMED)
      }
    })
    pending.foreach(transaction => {
      if (transaction.isDefined) {
        stateUpdateMap.put(transaction.get.id, TransactionStateStatus.PENDING)
      }
    })
    stateUpdateMap.toMap
  }

  private def getTransactionStateForRetry(
      transactionState: TransactionState
  ): TransactionState = {
    val retryCount = transactionState.retryCount + 1
    if (retryCount == MAX_RETRY_COUNT) {
      TransactionState(
        transactionState.id,
        transactionState.transactionId,
        TransactionStateStatus.FAILED,
        retryCount
      )
    } else {
      TransactionState(
        transactionState.id,
        transactionState.transactionId,
        transactionState.status,
        retryCount
      )
    }
  }

  private def getTransactionUpdatedStateIfRequired(
      transactionState: TransactionState,
      status: TransactionStateStatus.Value
  ): TransactionState = {
    TransactionState(
      transactionState.id,
      transactionState.transactionId,
      status,
      transactionState.retryCount
    )
  }

  private def notifyEventsSystem(
      transactionId: String,
      transaction: Option[Transaction],
      status: TransactionStateStatus.Value
  ): Unit = {
    if (status == TransactionStateStatus.CONFIRMED) {
      if (transaction.isEmpty) {
        logger.error(
          s"Confirmed transaction has empty transaction body: $transactionId"
        )
        return
      }
      eventProcessor.processConfirmedTransaction(transaction.get)
    } else if (status == TransactionStateStatus.FAILED) {
      eventProcessor.processFailedTransaction(transactionId)
    }
  }
}
