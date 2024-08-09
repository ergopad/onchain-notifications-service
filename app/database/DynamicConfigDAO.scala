package database

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext

import slick.jdbc.JdbcProfile

import database.JsonPostgresProfile.api._
import model._

@Singleton
class DynamicConfigDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {

  def get(key: String) = {
    val query =
      DynamicConfigStore.dynamicConfig.filter(_.key === key).result.headOption
    db.run(query)
  }

  def put(key: String, config: String) = {
    val query =
      DynamicConfigStore.dynamicConfig.insertOrUpdate(
        DynamicConfig(key, config)
      )
    db.run(query)
  }
}

object DynamicConfigStore {
  class DynamicConfigStore(tag: Tag)
      extends Table[DynamicConfig](tag, "dynamic_config") {
    def key = column[String]("key", O.PrimaryKey)
    def config = column[String]("config")
    def * = (
      key,
      config
    ) <> ((DynamicConfig.apply _).tupled, DynamicConfig.unapply)
  }

  val dynamicConfig = TableQuery[DynamicConfigStore]
}
