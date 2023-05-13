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

class TransactionPollerTask @Inject() (
    protected val ergoNodeClient: ErgoNodeClient,
    protected val transactionsDAO: TransactionsDAO,
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
      transactionStates.foreach(transactionState => {
        val updateDone = updateDBIfRequired(transactionState)
        if (updateDone) {
          notifyEventsSystem(transactionState.transactionId);
        }
      })
    } catch {
      case e: Exception => logger.error(e.getMessage())
    }
  )

  private def buildTransactionStates(
      mTransactions: Seq[MTransaction]
  ): Seq[TransactionState] = {
    mTransactions.map(transaction =>
      TransactionState(
        UUID.randomUUID,
        transaction.id,
        TransactionStateStatus.PENDING,
        0
      )
    )
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

  private def notifyEventsSystem(transactionId: String) = {
    logger.info(transactionId + ": " + TransactionStateStatus.PENDING.toString)
  }
}
