package models.ergoplatform

import play.api.libs.json.Json

final case class Token(
    tokenId: String,
    index: Int,
    amount: Long,
    name: String,
    decimals: Int,
) 

object Token {
    implicit val json = Json.format[Token]
}
