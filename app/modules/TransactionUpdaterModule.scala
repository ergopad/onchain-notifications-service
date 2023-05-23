package modules

import play.api.inject._

import tasks.TransactionUpdaterTask

class TransactionUpdaterModule extends SimpleModule(bind[TransactionUpdaterTask].toSelf.eagerly())
