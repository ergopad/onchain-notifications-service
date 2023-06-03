package model.ergoplatform

import play.api.libs.json.Json

final case class Miner(
    address: String,
    name: Option[String]
)

object Miner {
  implicit val json = Json.format[Miner]
}

final case class Block(
    id: String,
    height: Int,
    timestamp: Long,
    transactionsCount: Int,
    miner: Miner,
    size: Int,
    difficulty: Long,
    minerReward: Long
)

object Block {
  implicit val json = Json.format[Block]
}
