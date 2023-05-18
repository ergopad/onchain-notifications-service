package processor.plugins

import models._
import models.ergoplatform._

trait EventProcessorPlugin {

  /** Check if given mempool transaction should be processed by plugin
    */
  def isMatchingMempoolTransaction(transaction: MTransaction): Boolean

  /** Check if given transaction should be processed by plugin
    */
  def isMatchingTransaction(transaction: Transaction): Boolean

  /** Process the mempool transaction and generate the required
    * notifcations/events - Note: method is only called if
    * isMatchingMempoolTransaction returns true for the same transaction
    */
  def processMempoolTransaction(transaction: MTransaction): Seq[Event]

  /** Process the confirmed transaction and generate the required
    * notifcations/events - Note: method is only called if isMatchingTransaction
    * returns true for the same transaction
    */
  def processTransaction(transaction: Transaction): Seq[Event]
}
