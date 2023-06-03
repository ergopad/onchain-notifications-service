package model

import java.util.UUID

import play.api.libs.json.Json

final case class BlockHeight(
    id: UUID,
    blockHeight: Int
);

object BlockHeight {
  implicit val json = Json.format[BlockHeight]
}
