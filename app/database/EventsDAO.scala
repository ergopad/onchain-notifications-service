package database

import java.time.Instant
import java.util.UUID

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import slick.jdbc.JdbcProfile

import database.JsonPostgresProfile.api._
import model._

@Singleton
class EventsDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {

  def getAll: Future[Seq[Event]] = {
    val query = Events.events.result
    db.run(query)
  }

  def getEventById(id: UUID): Future[Option[Event]] = {
    val query =
      Events.events.filter(_.id === id).result.headOption
    db.run(query)
  }

  def getEventsForAddress(address: String): Future[Seq[Event]] = {
    val query = Events.events
      .filter(_.address === address)
      .sortBy(_.timestamp.desc)
      .result
    db.run(query)
  }

  def getEventsForPlugin(plugin: String, limit: Int = 100): Future[Seq[Event]] = {
    val query = Events.events
      .filter(_.pluginName === plugin)
      .sortBy(_.timestamp.desc)
      .take(limit)
      .result
    db.run(query)
  }

  def getEventsForAddressAndPluginName(
      address: String,
      pluginName: String
  ): Future[Seq[Event]] = {
    val query = Events.events
      .filter(_.address === address)
      .filter(_.pluginName === pluginName)
      .sortBy(_.timestamp.desc)
      .result
    db.run(query)
  }

  def createEvent(event: Event): Future[Any] = {
    val query = Events.events += event
    db.run(query)
  }
}

object Events {
  class Events(tag: Tag) extends Table[Event](tag, "events") {
    def id = column[UUID]("id", O.PrimaryKey)
    def pluginName = column[String]("plugin_name")
    def transactionId = column[String]("transaction_id")
    def address = column[String]("address")
    def body = column[JsValue]("body")
    def timestamp = column[Instant]("timestamp")
    def * = (
      id,
      pluginName,
      transactionId,
      address,
      body,
      timestamp
    ) <> ((Event.apply _).tupled, Event.unapply)
  }

  val events = TableQuery[Events]
}
