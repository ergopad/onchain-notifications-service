package util

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Configuration
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.json.JsError
import play.api.libs.ws.WSClient

import scala.concurrent.duration.Duration
import scala.concurrent.Await

import model.ergoplatform._

@Singleton
class ErgoNodeClient @Inject() (
    protected val ws: WSClient,
    protected val config: Configuration
) extends Logging {
  private val ERGONODE_URL = config.get[String]("ergonode.url")
  private val EXPLORER_URL = config.get[String]("explorer.url")
  private val HTTP_404_NOT_FOUND = 404;
  private val HTTP_200_OK = 200;

  def getMempoolTransactions: Seq[MTransaction] = {
    val request = ws
      .url(EXPLORER_URL + "/transactions/unconfirmed?limit=500")
      .get()
    val response = Await.result(request, Duration.Inf)
    if (response.status != HTTP_200_OK) {
      logger.error(
        "getMempoolTransactions failed with status: " + response.status + " and body: " + response.body
      )
    }
    val transactionsJson = response.json.\("items").get
    val result = Json.fromJson[Seq[MTransaction]](transactionsJson)
    if (result.isError) {
      logger.error(result.asInstanceOf[JsError].errors.toString)
      return Seq()
    }
    result.get
  }

  def getUnconfirmedTransaction(transactionId: String): Option[MTransaction] = {
    val request = ws
      .url(EXPLORER_URL + s"/transactions/unconfirmed/$transactionId")
      .get()
    val response = Await.result(request, Duration.Inf)
    if (
      response.status != HTTP_200_OK && response.status != HTTP_404_NOT_FOUND
    ) {
      logger.error(
        "getUnconfirmedTransaction failed with status: " + response.status + " and body: " + response.body
      )
    }
    if (response.status == HTTP_404_NOT_FOUND) {
      return Option.empty[MTransaction]
    }
    val transactionJson = response.json
    val result = Json.fromJson[MTransaction](transactionJson)
    if (result.isError) {
      logger.error(result.asInstanceOf[JsError].errors.toString)
    }
    result.asOpt
  }

  def getTransaction(transactionId: String): Option[Transaction] = {
    val request = ws
      .url(EXPLORER_URL + s"/api/v1/transactions/$transactionId")
      .get()
    val response = Await.result(request, Duration.Inf)
    if (
      response.status != HTTP_200_OK && response.status != HTTP_404_NOT_FOUND
    ) {
      logger.error(
        "getTransaction failed with status: " + response.status + " and body: " + response.body
      )
    }
    if (response.status == HTTP_404_NOT_FOUND) {
      return Option.empty[Transaction]
    }
    val transactionJson = response.json
    val result = Json.fromJson[Transaction](transactionJson)
    if (result.isError) {
      logger.error(result.asInstanceOf[JsError].errors.toString)
    }
    result.asOpt
  }

  def getLatestBlock: Option[Block] = {
    val request = ws
      .url(
        EXPLORER_URL + s"/blocks?limit=1&offset=0&sortBy=height&sortDirection=desc"
      )
      .get()
    val response = Await.result(request, Duration.Inf)
    if (response.status != HTTP_200_OK) {
      logger.error(
        "getLatestBlock failed with status: " + response.status + " and body: " + response.body
      )
    }
    val blocksJson = response.json.\("items").get
    val result = Json.fromJson[Seq[Block]](blocksJson)
    if (result.isError) {
      logger.error(result.asInstanceOf[JsError].errors.toString)
      return None
    }
    val blocks = result.get
    if (blocks.isEmpty) {
      return None
    }
    Option(blocks.head)
  }
}
