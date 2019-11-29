package com.github.onlinestorecqrs.framework.shard

import akka.pattern.ask
import akka.util.Timeout
import com.github.onlinestorecqrs.framework.api.CommandGateway
import com.github.onlinestorecqrs.framework.shard.CommandGatewayImpl.logger
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

object CommandGatewayImpl {
    val logger = LoggerFactory.getLogger(classOf[CommandGatewayImpl])
}

@Singleton
class CommandGatewayImpl @Inject()(
    shardRegionsByName: ShardRegionMap
) extends CommandGateway {

    override def send[T](aggregateName: String, msg: Any): Future[Any] = {
        val actorRefOption = shardRegionsByName.get(aggregateName)
        if (actorRefOption.isEmpty) {
            throw new IllegalArgumentException(s"Could not find any Aggregate named $aggregateName")
        }

        implicit val timeout = Timeout(2 seconds)

        logger.info("Sending {} to Aggregate {}", msg, aggregateName)
        actorRefOption.get ? msg
    }
}
