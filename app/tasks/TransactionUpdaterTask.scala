package tasks

import akka.actor.ActorSystem

import java.util.UUID

import javax.inject.Inject

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import slick.basic.DatabasePublisher
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import models._
import models.ergoplatform._
import database._
import util._

class TransactionUpdaterTask @Inject() (
    protected val ergoNodeClient: ErgoNodeClient,
    protected val transactionsDAO: TransactionsDAO,
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
          transactionState.status
        );
      })
    } catch {
      case e: Exception => logger.error(e.getMessage())
    }
  )

  private def getConfirmedTransactions(
      transactionStates: Seq[TransactionState]
  ): Seq[Option[Transaction]] = {
    transactionStates.map(transactionState =>
      ergoNodeClient.getTransaction(transactionState.transactionId)
    )
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
      scala.collection.mutable.Map[String, TransactionStateStatus.Value]();
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
      status: TransactionStateStatus.Value
  ) = {
    if (
      Seq(TransactionStateStatus.FAILED, TransactionStateStatus.CONFIRMED)
        .contains(status)
    ) {
      logger.info(transactionId + ": " + status.toString)
    }
  }
}
