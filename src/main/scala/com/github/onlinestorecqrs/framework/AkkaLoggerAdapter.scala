package com.github.onlinestorecqrs.framework

import akka.actor.ActorLogging
import com.github.onlinestorecqrs.framework.api.AggregateLogger

class AkkaLoggerAdapter(actorLogging: ActorLogging) extends AggregateLogger {
    override def info(message: String): Unit = actorLogging.log.info(message)
}
