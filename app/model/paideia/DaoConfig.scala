package model.paideia

import play.api.libs.json.Json

final case class DaoConfig(
    changeStake: String,
    vote: String,
    stake: String,
    unstake: String,
    dao: String,
)

object DaoConfig {
  implicit val json = Json.format[DaoConfig]
}
