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
import model._

@Singleton
class BlockHeightsDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {

  def getAll: Future[Seq[BlockHeight]] = {
    val query = BlockHeights.blockHeights.result
    db.run(query)
  }

  def getBlockHeight: Future[Option[BlockHeight]] = {
    val query = BlockHeights.blockHeights.result.headOption
    db.run(query)
  }

  def insertBlockHeight(height: Int): Future[Any] = {
    val query =
      BlockHeights.blockHeights += BlockHeight(UUID.randomUUID, height)
    db.run(query)
  }

  def updateBlockHeight(height: Int): Future[Any] = {
    val query =
      for (dbBlockHeightState <- BlockHeights.blockHeights)
        yield dbBlockHeightState.blockHeight
    db.run(
      query.update(height)
    ) map { _ > 0 }
  }
}

object BlockHeights {
  class BlockHeights(tag: Tag) extends Table[BlockHeight](tag, "block_height") {
    def id = column[UUID]("id", O.PrimaryKey)
    def blockHeight = column[Int]("block_height")
    def * = (
      id,
      blockHeight
    ) <> ((BlockHeight.apply _).tupled, BlockHeight.unapply)
  }

  val blockHeights = TableQuery[BlockHeights]
}
