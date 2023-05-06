package tasks

import akka.actor.ActorSystem

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import javax.inject.Inject

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
) extends HasDatabaseConfigProvider[JdbcProfile] {
  actorSystem.scheduler.scheduleOnce(delay = 5.seconds)(
    try {
      println("Running Startup")
      val schema = TableQuery[TransactionStates.TransactionStates].schema
      Await.result(
        db.run(DBIO.seq(schema.create)),
        Duration.Inf
      )
      println("Startup Done")
    } catch {
      case e: Exception => println(e)
    }
  )
}
