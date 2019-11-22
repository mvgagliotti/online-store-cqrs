package com.github.onlinestorecqrs

import akka.actor.ActorSystem
import com.github.onlinestorecqrs.domain.DomainApi.Get
import com.github.onlinestorecqrs.domain.OrderAggregate
import com.github.onlinestorecqrs.framework.api.{AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.module.CQRSModule
import com.github.onlinestorecqrs.shard.OrderShardRegion
import com.google.inject.{Guice, Provides}
import com.typesafe.config.ConfigFactory
import javax.inject.Singleton


object MainWithDI extends App {


    val module = new CQRSModule {

        @Provides
        @Singleton
        def system(): ActorSystem = {
            //creating actor system with all the persistence and cluster configuration
            val config = ConfigFactory.parseString(
                s"""
                   |akka.remote.artery.canonical.port = 2551
                """.stripMargin).withFallback(ConfigFactory.load("app.conf"))
            ActorSystem("OnlineStore", config)
        }

        override def aggregateDefinitions(): List[AggregateDefinition] = {
            List(
                new AggregateDefinition(
                    name = "Order",
                    aggregateClass = classOf[OrderAggregate],
                    idExtractor = OrderShardRegion.extractEntityId,
                    shardIdExtractor = OrderShardRegion.extractShardId)
            )
        }
    }


    val injector = Guice.createInjector(module)

    val commandGateway = injector.getInstance(classOf[CommandGateway])

    println(commandGateway)

    commandGateway.send("Order", Get("c1d5e35d-af24-4d94-866e-b61e5b93d9ab"))
}
