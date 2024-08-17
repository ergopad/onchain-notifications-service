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
import slick.jdbc.JdbcProfile
import database._
import processor.plugins._

import scala.concurrent.Await

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
    plugins.asScala.foreach(plugin => {
      try {
        val config = plugin.getConfig
        Await
          .result(dynamicConfigDAO.put(config.key, config.config), Duration.Inf)
      } catch {
        case e: Exception => logger.error(e.getMessage(), e)
      }
    })
  })
}
