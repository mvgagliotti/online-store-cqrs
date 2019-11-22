package com.github.onlinestorecqrs.framework.shard

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.github.onlinestorecqrs.framework.AggregatePersistentActor
import com.github.onlinestorecqrs.framework.api.Aggregate
import com.github.onlinestorecqrs.framework.api.AggregateDefinition.{IdExtractor, ShardIdExtractor}
import com.github.onlinestorecqrs.framework.module.InjectorDelegate
import javax.inject.Inject

case class ShardedAggregateFactory[T <: Aggregate](
    aggregateName: String,
    aggregateClass: Class[T],
    idExtractor: IdExtractor,
    shardIdExtractor: ShardIdExtractor,
    injector: InjectorDelegate
) {

    val aggregateFactory = new AggregateFactory[T](aggregateClass, injector)

    def build(actorSystem: ActorSystem) = {
        ClusterSharding(actorSystem)
            .start(
                typeName = aggregateName,
                Props(new AggregatePersistentActor[T](aggregateFactory)),
                settings = ClusterShardingSettings(actorSystem),
                extractEntityId = idExtractor,
                extractShardId = shardIdExtractor
            )
    }
}

case class AggregateFactory[T <: Aggregate](
    aggregateClass: Class[T],
    injector: InjectorDelegate
) {

    def build(objectsToInject: List[Object]): T = {
        //reflecting constructor and its arguments types
        val constructor = aggregateClass
            .getConstructors()
            .filter { x => x.isAnnotationPresent(classOf[Inject]) }.headOption

        if (constructor.isEmpty) {
            throw new IllegalStateException("Couldn't find any constructor with Inject annotation")
        }

        val types = constructor.get.getParameterTypes

        //getting constructor arguments
        val arguments = new Array[Object](types.length)
        for (i <- 0 until types.length) {

            val argumentClass = types(i).asInstanceOf[Class[Object]]
            val option =
                objectsToInject.filter { obj =>
                    argumentClass.isAssignableFrom(obj.getClass)
                }.headOption

            arguments(i) = if (option.isDefined) option.get
            else injector.getInstance(argumentClass)
        }

        //instantiating it
        val aggregateInstance = constructor.get.newInstance(arguments: _*)
        aggregateInstance.asInstanceOf[T]
    }

}
