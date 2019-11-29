package com.github.onlinestorecqrs.dimodules

import akka.actor.ActorSystem
import com.github.onlinestorecqrs.domain.OrderAggregate
import com.github.onlinestorecqrs.framework.api.{AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.module.CQRSModule
import com.github.onlinestorecqrs.http.{HttpRoutes, HttpServerStarter}
import com.github.onlinestorecqrs.shard.OrderShardRegion
import com.google.inject.Provides
import com.typesafe.config.ConfigFactory
import javax.inject.Singleton

class MainModule(clusterPort: Int) extends CQRSModule {

    @Provides
    @Singleton
    def system(): ActorSystem = {
        //creating actor system with all the persistence and cluster configuration
        val config = ConfigFactory.parseString(
            s"""
               |akka.remote.artery.canonical.port = ${clusterPort}
                """.stripMargin).withFallback(ConfigFactory.load("app.conf"))
        ActorSystem("OnlineStore", config)
    }

    @Provides
    @Singleton
    def httpRoutes(actorSystem: ActorSystem, commandGateway: CommandGateway): HttpRoutes = {
        new HttpRoutes(actorSystem, commandGateway)
    }

    @Provides
    @Singleton
    def httpServer(implicit actorSystem: ActorSystem, routes: HttpRoutes): HttpServerStarter = {
        new HttpServerStarter(actorSystem, routes)
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
