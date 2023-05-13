package tasks

import play.api.inject._

class TransactionPollerModule extends SimpleModule(bind[TransactionPollerTask].toSelf.eagerly())
