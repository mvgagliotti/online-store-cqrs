package com.github.onlinestorecqrs.framework

import akka.actor.ActorLogging

class AkkaLoggerAdapter(actorLogging: ActorLogging) extends AggregateLogger {
    override def info(message: String): Unit = actorLogging.log.info(message)
}
