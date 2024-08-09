package processor.plugins

import model._

trait DynamicConfigPlugin {

  /** Get updated config
    */
  def getConfig: DynamicConfig
}
