package modules

import play.api.inject._

import tasks.TransactionPollerTask

class TransactionPollerModule extends SimpleModule(bind[TransactionPollerTask].toSelf.eagerly())
