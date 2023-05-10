package util

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.collection.JavaConverters
import scala.concurrent.duration.Duration
import scala.concurrent.Await

import models.ergoplatform._

@Singleton
class ErgoNodeClient @Inject() (private val ws: WSClient, private val config: Configuration) {
    private val ERGONODE_URL = config.get[String]("ergonode.url")
    private val EXPLORER_URL = config.get[String]("explorer.url")
    private val HTTP_404_NOT_FOUND = 404;
    
    def getMempoolTransactions: Seq[MTransaction] = {
        val request = ws
            .url(EXPLORER_URL + "/transactions/unconfirmed")
            .get()
        val response = Await.result(request, Duration.Inf)
        val transactionsJson = response.json.\("items").get
        Json.fromJson[Seq[MTransaction]](transactionsJson).get
    }

    def getUnconfirmedTransaction(transactionId: String): Option[MTransaction] = {
        val request = ws
            .url(EXPLORER_URL + s"/transactions/unconfirmed/$transactionId")
            .get()
        val response = Await.result(request, Duration.Inf)
        if (response.status == HTTP_404_NOT_FOUND) {
            return Option.empty[MTransaction]
        }
        val transactionJson = response.json
        Json.fromJson[MTransaction](transactionJson).asOpt
    }

    def getTransaction(transactionId: String): Option[Transaction] = {
        val request = ws
            .url(EXPLORER_URL + s"/api/v1/transactions/$transactionId")
            .get()
        val response = Await.result(request, Duration.Inf)
        if (response.status == HTTP_404_NOT_FOUND) {
            return Option.empty[Transaction]
        }
        val transactionJson = response.json
        Json.fromJson[Transaction](transactionJson).asOpt
    }
}
