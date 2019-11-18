package com.github.onlinestorecqrs.shard

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.github.onlinestorecqrs.domain.persistence.OrderActor
import com.github.onlinestorecqrs.domain.persistence.OrderActor.{CreateOrderCommand, OrderCommand}

object OrderShardRegion {

    val NUMBER_OF_SHARDS = 10

    def setupClusterSharding(system: ActorSystem) = {
        ClusterSharding(system).start(
            typeName = "Order",
            entityProps = Props(new OrderActor()),
            settings = ClusterShardingSettings(system),
            extractEntityId = extractEntityId,
            extractShardId = extractShardId
        )
    }

    def extractEntityId: ShardRegion.ExtractEntityId = superClassExtractEntityId

    def extractShardId: ShardRegion.ExtractShardId = { command =>
        val orderCommand: OrderCommand = command.asInstanceOf[OrderCommand]
        (orderCommand.orderId.hashCode.abs.intValue() % NUMBER_OF_SHARDS).toString
    }

    private def superClassExtractEntityId =
        new IsSuperClassOf[OrderCommand, (String, OrderCommand)](classOf[OrderCommand]) {
            override def apply(command: Any): (String, OrderCommand) = {
                val orderCommand: OrderCommand = command.asInstanceOf[OrderCommand]
                (orderCommand.orderId, orderCommand)
            }
        }

    private abstract class IsSuperClassOf[A, B](clazz: Class[A]) extends PartialFunction[Any, B] {
        override def isDefinedAt(x: Any): Boolean = {
            if (clazz.isAssignableFrom(x.getClass)) true else false
        }
    }

}
