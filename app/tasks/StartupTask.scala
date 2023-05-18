package tasks

import akka.actor.ActorSystem

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

class StartupTask @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val actorSystem: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile]
    with Logging {
  actorSystem.scheduler.scheduleOnce(delay = 2.seconds)(
    try {
      logger.info("Running Startup")
      val schema = TableQuery[TransactionStates.TransactionStates].schema ++
        TableQuery[Events.Events].schema
      Await.result(
        db.run(DBIO.seq(schema.create)),
        Duration.Inf
      )
      logger.info("Startup Done")
    } catch {
      case e: Exception => logger.error(e.getMessage(), e)
    }
  )
}
