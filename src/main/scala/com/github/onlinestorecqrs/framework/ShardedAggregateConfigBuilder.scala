package com.github.onlinestorecqrs.framework

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

class ShardedAggregateConfigBuilder[T <: Aggregate[R], R] {

    type IdExtractor = PartialFunction[Any, (String, Any)]
    type ShardIdExtractor = Any => String

    private val aggregateBuilder: AggregateBuilder[T, R] = new AggregateBuilder[T, R]()
    private var aggregateName: String = null
    private var idExtractor: IdExtractor = null
    private var shardIdExtractor: ShardIdExtractor = null

    def buildShardRegion(actorSystem: ActorSystem): ActorRef = {
        //TODO: validate builder fields

        ClusterSharding(actorSystem)
            .start(
                typeName = aggregateName,
                Props(new AggregatePersistentActor[T, R](aggregateBuilder)),
                settings = ClusterShardingSettings(actorSystem),
                extractEntityId = idExtractor,
                extractShardId = shardIdExtractor
            )
    }

    def withName(name: String) = {
        aggregateName = name
        this
    }

    def withInstanceCreator(x: ((EventManager, AggregateLogger) => T)) = {
        aggregateBuilder.withInstanceCreator(x)
        this
    }

    def withEntityIdExtractor(x: IdExtractor) = {
        idExtractor = x
        this
    }

    def withShardIdExtractor(x: ShardIdExtractor) = {
        shardIdExtractor = x
        this
    }
}

class AggregateBuilder[T <: Aggregate[R], R] {

    private var instanceCreator: ((EventManager, AggregateLogger) => T) = null

    def withInstanceCreator(x: ((EventManager, AggregateLogger) => T)) = {
        instanceCreator = x
        this
    }

    def build(eventManager: EventManager, aggregateLogger: AggregateLogger): T = {
        instanceCreator(eventManager, aggregateLogger)
    }
}
