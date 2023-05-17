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

import database._
import models._
import models.ergoplatform._
import processor._
import util._

class TransactionPollerTask @Inject() (
    protected val ergoNodeClient: ErgoNodeClient,
    protected val transactionsDAO: TransactionsDAO,
    protected val eventProcessor: EventProcessorCore,
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val actorSystem: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {
  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 5.seconds,
    delay = 60.seconds
  )(() =>
    try {
      val mTransactions = ergoNodeClient.getMempoolTransactions
      val transactionStates = buildTransactionStates(mTransactions)
      transactionStates.foreach(transaction => {
        val updateDone = updateDBIfRequired(transaction._1)
        if (updateDone) {
          notifyEventsSystem(transaction._2);
        }
      })
    } catch {
      case e: Exception => logger.error(e.getMessage())
    }
  )

  private def buildTransactionStates(
      mTransactions: Seq[MTransaction]
  ): Seq[(TransactionState, MTransaction)] = {
    mTransactions.map(transaction => {
      val transactionState = TransactionState(
        UUID.randomUUID,
        transaction.id,
        TransactionStateStatus.PENDING,
        0
      )
      // tuple of transaction state and mempool transaction
      (transactionState, transaction)
    })
  }

  private def updateDBIfRequired(
      transactionState: TransactionState
  ): Boolean = {
    val alreadyExists = Await
      .result(
        transactionsDAO.getTransaction(transactionState.transactionId),
        Duration.Inf
      )
      .isDefined;
    if (!alreadyExists) {
      Await.result(
        transactionsDAO.insertTransactionState(transactionState),
        Duration.Inf
      );
    }
    !alreadyExists
  }

  private def notifyEventsSystem(transaction: MTransaction) = {
    eventProcessor.processPendingTransaction(transaction);
  }
}
