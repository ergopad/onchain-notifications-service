package processor.plugins

import javax.inject.Inject
import javax.inject.Singleton

import model.paideia._
import model._
import util._

import play.api.libs.json.Json
import play.api.Logging

/** Plugin to Detect bPaideia Proposal Events
  */
@Singleton
class PaideiaConfigPlugin @Inject() (
    protected val client: PaideiaClient
) extends DynamicConfigPlugin
    with Logging {

  /** Get Paideia contracts config
    */
  def getConfig: DynamicConfig = {
    logger.info("Updating dynamic config for Paideia contracts")
    val daos: Seq[Dao] = client.getDaos
    val config = daos
      .map(dao => client.getDaoConfig(dao.daoUrl))
      .filter(maybeConfig => maybeConfig.isDefined)
      .map(maybeConfig => maybeConfig.get)
    val configJson = Json.stringify(Json.toJson(config))
    logger.info("Updated config: " + configJson)
    DynamicConfig(
      "PaideiaConfigPlugin",
      configJson
    )
  }
}
