package modules

import play.api.inject._

import tasks.StartupTask

class StartupModule extends SimpleModule(bind[StartupTask].toSelf.eagerly())
