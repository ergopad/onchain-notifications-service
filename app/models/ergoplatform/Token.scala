package models.ergoplatform

import play.api.libs.json.Json

final case class Token(
    tokenId: String,
    index: Int,
    amount: Long,
    name: Option[String],
    decimals: Option[Int],
) 

object Token {
    implicit val json = Json.format[Token]
}
