package modules

import play.api.inject._

import tasks.DynamicConfigUpdaterTask

class DynamicConfigUpdaterModule
    extends SimpleModule(bind[DynamicConfigUpdaterTask].toSelf.eagerly())
