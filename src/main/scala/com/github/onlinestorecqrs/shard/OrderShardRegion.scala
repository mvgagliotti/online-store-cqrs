package com.github.onlinestorecqrs.shard

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.github.onlinestorecqrs.domain.persistence.OrderActor
import com.github.onlinestorecqrs.domain.persistence.OrderActor.OrderCommand

object OrderShardRegion {

    val NUMBER_OF_SHARDS = 10

    def setupClusterSharding(system: ActorSystem): ActorRef = {
        ClusterSharding(system).start(
            typeName = "Order",
            entityProps = Props[OrderActor],
            settings = ClusterShardingSettings(system),
            extractEntityId = extractEntityId,
            extractShardId = extractShardId
        )
    }

    private def extractEntityId: ShardRegion.ExtractEntityId = superClassExtractEntityId

    private def extractShardId: ShardRegion.ExtractShardId = superClassExtractShardID

    private def superClassExtractShardID =
        new IsSuperClassOf[OrderCommand, String](classOf[OrderCommand]) {
            override def apply(command: Any): String = command match {
                case orderCommand:OrderCommand => (orderCommand.orderId.hashCode.intValue() % NUMBER_OF_SHARDS).toString
            }
    }

    private def superClassExtractEntityId =
        new IsSuperClassOf[OrderCommand, (String, OrderCommand)](classOf[OrderCommand]) {
            override def apply(command: Any): (String, OrderCommand) = command match {
                case orderCommand:OrderCommand => (orderCommand.orderId, orderCommand)
            }
        }

    private abstract class IsSuperClassOf[A, B](clazz: Class[A]) extends PartialFunction[Any, B] {
        override def isDefinedAt(x: Any): Boolean = clazz.isAssignableFrom(x.getClass)
    }

}
