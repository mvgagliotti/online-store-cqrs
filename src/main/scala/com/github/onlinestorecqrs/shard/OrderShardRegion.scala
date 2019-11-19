package com.github.onlinestorecqrs.shard

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.ShardRegion
import com.github.onlinestorecqrs.domain.DomainModel.Order
import com.github.onlinestorecqrs.domain.OrderAggregate
import com.github.onlinestorecqrs.domain.DomainApi.OrderCommand
import com.github.onlinestorecqrs.framework.ShardedAggregateConfigBuilder

object OrderShardRegion {

    val NUMBER_OF_SHARDS = 10

    def setupClusterSharding(system: ActorSystem): ActorRef = {
        new ShardedAggregateConfigBuilder[OrderAggregate, Order]()
            .withName("Order")
            .withInstanceCreator((new OrderAggregate(_, _)))
            .withEntityIdExtractor(OrderShardRegion.extractEntityId)
            .withShardIdExtractor(OrderShardRegion.extractShardId)
            .buildShardRegion(system)
    }

    def extractEntityId: ShardRegion.ExtractEntityId = superClassExtractEntityId

    def extractShardId: ShardRegion.ExtractShardId = { command =>
        val orderCommand: OrderCommand = command.asInstanceOf[OrderCommand]
        (orderCommand.orderId.hashCode.abs.intValue() % NUMBER_OF_SHARDS).toString
    }

    private def superClassExtractEntityId =
        new IsSuperClassOf[OrderCommand, (String, OrderCommand)](classOf[OrderCommand]) {
            override def apply(command: Any): (String, OrderCommand) = command match {
                case orderCommand: OrderCommand => (orderCommand.orderId, orderCommand)
            }
        }

    private abstract class IsSuperClassOf[A, B](clazz: Class[A]) extends PartialFunction[Any, B] {
        override def isDefinedAt(x: Any): Boolean = clazz.isAssignableFrom(x.getClass)
    }

}
