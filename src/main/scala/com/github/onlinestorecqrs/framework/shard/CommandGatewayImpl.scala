package com.github.onlinestorecqrs.framework.shard

import akka.actor.{ActorRef, ActorSystem}
import com.github.onlinestorecqrs.framework.api.{Aggregate, AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.module.InjectorDelegate
import javax.inject.{Inject, Singleton}

import scala.collection.mutable.Map
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

@Singleton
class CommandGatewayImpl @Inject()(
    actorSystem: ActorSystem,
    aggregateDefinitions: List[AggregateDefinition],
    injector: InjectorDelegate
) extends CommandGateway {

    private final val shardRegionsByName = buildAggregateShardRegions()

    def buildAggregateShardRegions(): Map[String, ActorRef] = {

        val map: Map[String, ActorRef] = Map()

        aggregateDefinitions.foreach { definition =>

            val actorRef = new ShardedAggregateFactory(
                aggregateName = definition.name,
                aggregateClass = definition.aggregateClass,
                idExtractor = definition.idExtractor,
                shardIdExtractor = definition.shardIdExtractor,
                injector = injector
            ).build(actorSystem)

            map.put(definition.name, actorRef)
        }

        map
    }

    //    def test() = {
    //
    //        val map: Map[String, ShardedAggregateFactory[_ <: Aggregate]] = Map()
    //
    //        aggregateDefinitions.foreach { definition =>
    //
    //
    //            //            new ShardedAggregateConfigBuilder()
    //            //                .withName(definition.name)
    //            //                .withEntityIdExtractor(definition.idExtractor)
    //            //                .withShardIdExtractor(definition.shardIdExtractor)
    //            //                .withClass(definition.aggregateClass)
    //            //                .buildShardRegion(null) //TODO: what actor system to pass????
    //
    //            val aggBuilder = new ShardedAggregateFactory(
    //                aggregateName = definition.name,
    //                aggregateClass = definition.aggregateClass,
    //                idExtractor = definition.idExtractor,
    //                shardIdExtractor = definition.shardIdExtractor,
    //                injector = injector
    //            )
    //
    //            map.put(definition.name, aggBuilder)
    //        }
    //
    //        map
    //    }

    override def send[T](aggregateName: String, msg: Any): Future[Any] = {
        val actorRef = shardRegionsByName.get(aggregateName)
        if (actorRef.isEmpty) {
            throw new IllegalArgumentException(s"Could not find any Aggregate named $aggregateName")
        }

        implicit val timeout = Timeout(2 seconds)

        actorRef.get ? msg
    }
}
