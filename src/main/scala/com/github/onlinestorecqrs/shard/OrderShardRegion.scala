package com.github.onlinestorecqrs.shard

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.github.onlinestorecqrs.domain.persistence.OrderActor
import com.github.onlinestorecqrs.domain.persistence.OrderActor.OrderCommand

object OrderShardRegion {

    val NUMBER_OF_SHARDS = 10

    case class CommandEnvelope(orderId: String, command: OrderCommand)

    def extractEntityId: ShardRegion.ExtractEntityId = {
        case CommandEnvelope(orderId, command) =>
            (orderId, command)
    }

    def extractShardId: ShardRegion.ExtractShardId = {
        case CommandEnvelope(orderId, _) =>
            (orderId.hashCode.intValue() % NUMBER_OF_SHARDS).toString
    }

    def setupClusterSharding(system: ActorSystem) = {
        ClusterSharding(system).start(
            typeName = "Order",
            entityProps = Props[OrderActor],
            settings = ClusterShardingSettings(system),
            extractEntityId = extractEntityId,
            extractShardId = extractShardId
        )
    }

}
