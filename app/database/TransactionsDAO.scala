package database

import java.util.UUID

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import slick.jdbc.JdbcProfile

import database.JsonPostgresProfile.api._
import models._

@Singleton
class TransactionsDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {

  def getAll: Future[Seq[TransactionState]] = {
    val query = TransactionStates.transactionStates.result
    db.run(query)
  }

  def getTransactionStateById(id: UUID): Future[Option[TransactionState]] = {
    val query =
      TransactionStates.transactionStates.filter(_.id === id).result.headOption
    db.run(query)
  }

  def getPendingTransactions: Future[Seq[TransactionState]] = {
    val query = TransactionStates.transactionStates
      .filter(_.status === TransactionStateStatus.PENDING)
      .result
    db.run(query)
  }

  def getTransaction(
      transactionId: String
  ): Future[Option[TransactionState]] = {
    val query = TransactionStates.transactionStates
      .filter(_.transactionId === transactionId)
      .result
      .headOption
    db.run(query)
  }

  def insertTransactionState(
      transactionState: TransactionState
  ): Future[Any] = {
    val query = TransactionStates.transactionStates += transactionState
    db.run(query)
  }

  def updateTransactionState(
      transactionState: TransactionState
  ): Future[Any] = {
    val query =
      for (
        dbTransactionState <- TransactionStates.transactionStates
        if dbTransactionState.id === transactionState.id
      )
        yield (
          dbTransactionState.transactionId,
          dbTransactionState.status,
          dbTransactionState.retryCount
        )
    db.run(
      query.update(
        transactionState.transactionId,
        transactionState.status,
        transactionState.retryCount
      )
    ) map { _ > 0 }
  }

  def deleteTransaction(id: UUID): Future[Any] = {
    val query = TransactionStates.transactionStates.filter(_.id === id).delete
    db.run(query)
  }
}

object TransactionStates {
  class TransactionStates(tag: Tag)
      extends Table[TransactionState](tag, "transaction_states") {
    def id = column[UUID]("id", O.PrimaryKey)
    def transactionId = column[String]("transaction_id")
    def status = column[TransactionStateStatus.Value]("status")
    def retryCount = column[Int]("retry_count")
    def * = (
      id,
      transactionId,
      status,
      retryCount
    ) <> ((TransactionState.apply _).tupled, TransactionState.unapply)
    def transactionIdIndex = index(
      "transaction_states_transaction_id_index",
      (transactionId),
      unique = true
    )
  }

  val transactionStates = TableQuery[TransactionStates]
}
