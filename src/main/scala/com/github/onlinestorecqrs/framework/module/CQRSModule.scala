package com.github.onlinestorecqrs.framework.module

import akka.actor.{ActorRef, ActorSystem}
import com.github.onlinestorecqrs.framework.api.{AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.shard.{CommandGatewayImpl, ShardRegionMap, ShardedAggregateFactory}
import com.google.inject.{AbstractModule, Injector, Provider, Provides}
import javax.inject.{Inject, Singleton}

import scala.collection.mutable.Map

/**
  * Module to help wiring components
  */
abstract class CQRSModule extends AbstractModule {

    def configureComponents(): Unit = {}

    def aggregateDefinitions(): List[AggregateDefinition]

    @Provides
    @Singleton
    final def providedDefinitions() = aggregateDefinitions()

    final override def configure(): Unit = {
        bind(classOf[InjectorDelegate]).toProvider(classOf[InjectorProvider])
        bind(classOf[ShardRegionMap]).toProvider(classOf[ShardRegionsProvider])
        bind(classOf[CommandGateway]).to(classOf[CommandGatewayImpl])
        configureComponents()
    }

}

@Singleton
class ShardRegionsProvider @Inject()(
    actorSystem: ActorSystem,
    definitions: List[AggregateDefinition],
    injector: InjectorDelegate
) extends Provider[ShardRegionMap] {

    private final val shardRegionMap = buildMap()

    def buildMap() = {
        val map: collection.mutable.Map[String, ActorRef] = Map()

        definitions.foreach { definition =>

            val actorRef = new ShardedAggregateFactory(
                aggregateName = definition.name,
                aggregateClass = definition.aggregateClass,
                idExtractor = definition.idExtractor,
                shardIdExtractor = definition.shardIdExtractor,
                injector = injector
            ).build(actorSystem)

            map.put(definition.name, actorRef)
        }

        new ShardRegionMap(map.toMap)
    }

    override def get(): ShardRegionMap = {
        shardRegionMap
    }
}

/**
  * Auxiliary provider to allow access of injector inside components
  *
  * @param injector Guice injector
  */
@Singleton
class InjectorProvider @Inject()(
    injector: Injector
) extends Provider[InjectorDelegate] {
    override def get(): InjectorDelegate = {
        new InjectorDelegate(injector)
    }
}
