package util

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Configuration
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.json.JsError
import play.api.libs.json.JsNull
import play.api.libs.ws.WSClient

import scala.collection.JavaConverters
import scala.concurrent.duration.Duration
import scala.concurrent.Await

import model.paideia._

@Singleton
class PaideiaClient @Inject() (
    protected val ws: WSClient,
    protected val config: Configuration
) extends Logging {
  private val PAIDEIA_API = config.get[String]("paideia.url")
  private val STAKE_CHANGE_KEY = "im.paideia.contracts.staking.changestake"
  private val VOTE_KEY = "im.paideia.contracts.staking.vote"
  private val STAKE_KEY = "im.paideia.contracts.staking.stake"
  private val UNSTAKE_KEY = "im.paideia.contracts.staking.unstake"
  private val DAO_KEY = "im.paideia.contracts.dao"
  private val HTTP_404_NOT_FOUND = 404
  private val HTTP_200_OK = 200

  def getDaos: Seq[Dao] = {
    val request = ws
      .url(PAIDEIA_API + "/dao/")
      .get()
    val response = Await.result(request, Duration.Inf)
    if (response.status != HTTP_200_OK) {
      logger.warn(
        "getDaos failed with status: " + response.status + " and body: " + response.body
      )
    }
    val daosJson = response.json
    val result = Json.fromJson[Seq[Dao]](daosJson)
    if (result.isError) {
      logger.error(result.asInstanceOf[JsError].errors.toString)
      return Seq()
    }
    result.get
  }

  def getDaoConfig(daoUrl: String): Option[DaoConfig] = {
    val request = ws
      .url(PAIDEIA_API + "/dao/" + daoUrl + "/config")
      .get()
    val response = Await.result(request, Duration.Inf)
    if (
      response.status != HTTP_200_OK && response.status != HTTP_404_NOT_FOUND
    ) {
      logger.warn(
        "getDaoConfig failed with status: " + response.status + " and body: " + response.body
      )
    }
    if (response.status == HTTP_404_NOT_FOUND) {
      return None
    }
    val configJson = response.json
    try {
      Option(
        DaoConfig(
          url = daoUrl,
          changeStake = configJson
            .\(STAKE_CHANGE_KEY)
            .\("value")
            .getOrElse(JsNull)
            .toString
            .stripPrefix("\"")
            .stripSuffix("\"")
            .trim,
          vote = configJson
            .\(VOTE_KEY)
            .\("value")
            .getOrElse(JsNull)
            .toString
            .stripPrefix("\"")
            .stripSuffix("\"")
            .trim,
          stake = configJson
            .\(STAKE_KEY)
            .\("value")
            .getOrElse(JsNull)
            .toString
            .stripPrefix("\"")
            .stripSuffix("\"")
            .trim,
          unstake = configJson
            .\(UNSTAKE_KEY)
            .\("value")
            .getOrElse(JsNull)
            .toString
            .stripPrefix("\"")
            .stripSuffix("\"")
            .trim,
          dao = configJson
            .\(DAO_KEY)
            .\("value")
            .getOrElse(JsNull)
            .toString
            .stripPrefix("\"")
            .stripSuffix("\"")
            .trim
        )
      )
    } catch {
      case e: Exception => {
        logger.error(e.getMessage(), e)
        None
      }
    }
  }
}
