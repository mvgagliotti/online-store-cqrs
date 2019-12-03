package com.github.onlinestorecqrs

import java.io.File

import akka.actor.ActorSystem
import akka.persistence.cassandra.testkit.CassandraLauncher
import akka.testkit.{ImplicitSender, TestKit}
import com.github.onlinestorecqrs.domain.DomainApi.{CreateOrderCommand, OrderCreatedEvent}
import com.github.onlinestorecqrs.domain.DomainModel.Item
import com.github.onlinestorecqrs.domain.OrderAggregate
import com.github.onlinestorecqrs.framework.api.{AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.module.CQRSModule
import com.github.onlinestorecqrs.http.HttpServerStarter
import com.github.onlinestorecqrs.shard.OrderShardRegion
import com.google.inject.{Guice, Injector, Provides}
import javax.inject.Singleton
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

final class OnlinestoreTest extends TestKit(ActorSystem("testActorSystem"))
    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll {

    val module = new CQRSModule {

        @Provides
        @Singleton
        def system(): ActorSystem = {
            OnlinestoreTest.this.system
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

    var injector: Injector = null
    var server: HttpServerStarter = null
    var commandGateway: CommandGateway = null

    override def beforeAll(): Unit = {
        CassandraLauncher.start(
            new File("/tmp/cassandra"),
            CassandraLauncher.DefaultTestConfigResource,
            false,
            9042,
            Nil,
            Some("localhost")
        )

        injector = Guice.createInjector(module)
        commandGateway = injector.getInstance(classOf[CommandGateway])

        Thread.sleep(10000)
    }

    override def afterAll(): Unit = {
        CassandraLauncher.stop()
        TestKit.shutdownActorSystem(system)
    }

    "An Aggregate Order" should {
        "create an Order when it receives a CreateOrderCommand" in {

            val command = new CreateOrderCommand(
                orderId = "123456",
                userId = "user-1",
                items = List(new Item(
                    id = "1",
                    description = "descr",
                    value = BigDecimal.valueOf(10.0),
                    amount = 1
                ))
            )

            val future = commandGateway.send("Order", command).mapTo[OrderCreatedEvent]
            val result: Try[OrderCreatedEvent] = Await.ready(future, 4 seconds).value.get

            assert(result.isSuccess)

        }
    }


}
