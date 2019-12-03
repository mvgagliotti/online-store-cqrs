package com.github.onlinestorecqrs.framework

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.persistence.cassandra.testkit.CassandraLauncher
import akka.testkit.{ImplicitSender, TestKit}
import com.github.onlinestorecqrs.framework.AggregatePersistentActorSec.{CreateCommand, CreatedEvent, MyAggregate}
import com.github.onlinestorecqrs.framework.api.{Aggregate, AggregateLogger, EventManager}
import com.github.onlinestorecqrs.framework.shard.AggregateFactory
import com.typesafe.config.ConfigFactory
import javax.inject.Inject
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

final class AggregatePersistentActorSec extends
    TestKit(ActorSystem("testActorSystem", ConfigFactory.load("AggregatePersistentActorSec.conf")))

    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll {

    override def beforeAll(): Unit = {
        CassandraLauncher.start(
            new File("/tmp/cassandra"),
            CassandraLauncher.DefaultTestConfigResource,
            true,
            9042,
        )
    }

    override def afterAll(): Unit = {
        CassandraLauncher.stop()
        TestKit.shutdownActorSystem(system)
    }

    "An aggregate persistent actor" should {
        "delegate a command to its aggregate, which should persist the generated event and send a notification" in {

            val factory = new AggregateFactory[MyAggregate](classOf[MyAggregate], null)
            val actorRef = system.actorOf(Props(new AggregatePersistentActor(factory)), "Instance_1")

            actorRef ! new CreateCommand("1", "value 1")

            expectMsg(new CreatedEvent("1", "value 1"))
        }
    }

}

object AggregatePersistentActorSec {

    case class AggregateRoot(id: String, value: String)

    case class CreateCommand(id: String, value: String)

    case class CreatedEvent(id: String, value: String)

    class MyAggregate @Inject()(
        eventManager: EventManager,
        logger: AggregateLogger
    ) extends Aggregate {
        var aggregateRoot: AggregateRoot = null

        override def handleCommand: CommandHandler = {
            case CreateCommand(id, value) =>
                logger.info("Persisting event")
                eventManager.persist(new CreatedEvent(id, value)) { persistedEvent =>
                    eventManager.notify(persistedEvent)
                }
        }

        override def handleEvent: EventHandler = {
            case evt@CreatedEvent(id, value) =>
                aggregateRoot = new AggregateRoot(id, value)
                logger.info(s"Handling event ${evt}")
        }
    }

}