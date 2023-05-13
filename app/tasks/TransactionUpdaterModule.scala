package tasks

import play.api.inject._

class TransactionUpdaterModule extends SimpleModule(bind[TransactionUpdaterTask].toSelf.eagerly())
