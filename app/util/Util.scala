package util

import org.ergoplatform.appkit.Address
import scorex.crypto.hash.Blake2b256

object Util {

  /** Get bytes from hex string
    */
  def hexToBase64(s: String): Array[Byte] = {
    val len = s.length
    val data = new Array[Byte](len / 2)
    var i = 0
    while ({
      i < len
    }) {
      data(i / 2) = ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(
        s.charAt(i + 1),
        16
      )).toByte
      i += 2
    }
    data
  }

  /** Get hex string from bytes
    */
  def bytesToHex(bytes: Seq[Byte]): String = {
    val sb = new StringBuilder
    for (b <- bytes) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  /** Hash ergo contract address using blake2b256 - generate contract signature
    */
  def pHashAddress(address: String): String = {
    val ergoAddress = Address.create(address)
    val ergoTree = ergoAddress.toErgoContract.getErgoTree
    bytesToHex(Blake2b256(ergoTree.bytes))
  }

  /** Parse contract hash from PaideiaContractSignature object
    *
    * Note: probably not the correct place for this method...
    */
  def parseHashFromPaideiaContractSignature(obj: String): String = {
    obj.split(",").lastOption.getOrElse("").stripSuffix(")")
  }
}
