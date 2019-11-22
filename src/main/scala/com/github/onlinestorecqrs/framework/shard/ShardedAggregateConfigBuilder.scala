package com.github.onlinestorecqrs.framework.shard

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.github.onlinestorecqrs.framework.api.{Aggregate, AggregateLogger, EventManager}
import com.github.onlinestorecqrs.framework.AggregatePersistentActor

/**
  * TODO: remove
  *
  * @tparam T
  */
@Deprecated
class ShardedAggregateConfigBuilder[T <: Aggregate] {

    type IdExtractor = PartialFunction[Any, (String, Any)]
    type ShardIdExtractor = Any => String

    private val aggregateBuilder: AggregateBuilder[T] = new AggregateBuilder[T]()
    private var aggregateName: String = null
    private var idExtractor: IdExtractor = null
    private var shardIdExtractor: ShardIdExtractor = null

    def buildShardRegion(actorSystem: ActorSystem): ActorRef = {
        //TODO: validate builder fields

        ClusterSharding(actorSystem)
            .start(
                typeName = aggregateName,
                Props(new AggregatePersistentActor[T](null)),
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

    def withClass(aggClass: Class[T]) = {
        aggregateBuilder.withClass(aggClass)
        this
    }

}

class AggregateBuilder[T <: Aggregate] {

    private var instanceCreator: ((EventManager, AggregateLogger) => T) = null
    private var aggregateClass: Class[T] = null

    def withInstanceCreator(x: ((EventManager, AggregateLogger) => T)) = {
        instanceCreator = x
        this
    }

    def withClass(aggClass: Class[T]) = {
        aggregateClass = aggClass
    }

    def build(eventManager: EventManager, aggregateLogger: AggregateLogger): T = {
        instanceCreator(eventManager, aggregateLogger)
    }
}
