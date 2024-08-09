package tasks

import akka.actor.ActorSystem

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import slick.basic.DatabasePublisher
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import database._
import model._
import processor.plugins._

@Singleton
class DynamicConfigUpdaterTask @Inject() (
    protected val dynamicConfigDAO: DynamicConfigDAO,
    protected val plugins: java.util.Set[DynamicConfigPlugin],
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val actorSystem: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {
  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 5.seconds,
    delay = 1.hour
  )(() => {
    logger.info("Running DynamicConfigUpdaterTask")
    try {
      plugins.asScala.foreach(plugin => {
        val config = plugin.getConfig
        dynamicConfigDAO.put(config.key, config.config)
      })
    } catch {
      case e: Exception => logger.error(e.getMessage(), e)
    }
  })
}
