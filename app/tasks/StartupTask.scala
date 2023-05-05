package tasks

import akka.actor.ActorSystem

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import slick.jdbc.JdbcProfile

class StartupTask @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val actorSystem: ActorSystem
) extends HasDatabaseConfigProvider[JdbcProfile] {
  actorSystem.scheduler.scheduleOnce(delay = 5.seconds)(
    try {
      println("Running startup")
      println("Startup done")
    } catch {
      case e: Exception => println(e)
    }
  )
}
