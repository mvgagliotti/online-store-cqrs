package com.github.onlinestorecqrs.framework

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.github.onlinestorecqrs.framework.api.{Aggregate, EventManager, Snapshot}
import com.github.onlinestorecqrs.framework.shard.{AggregateFactory}

/**
  * Aggregate Persistent Actor
  *
  * @param aggregateFactory
  * @tparam T
  */
class AggregatePersistentActor[T <: Aggregate](aggregateFactory: AggregateFactory[T]) extends PersistentActor
    with ActorLogging {

    //TODO: extract adapter
    val eventManagerAdapter = new EventManager {

        override def persist[A](event: A)(handler: A => Unit): Unit = {
            AggregatePersistentActor.super.persist(event)(handler)
        }

        override def notify[A](event: A): Unit = {
            aggregate.handleEvent(event)
            AggregatePersistentActor.super.sender() ! event
        }
    }

    val aggregate: T = aggregateFactory.build(List(eventManagerAdapter, new AkkaLoggerAdapter(this)))

    override def persistenceId: String = self.path.name.split("/")(self.path.name.split("/").length - 1)

    override def receiveCommand: Receive = aggregate.handleCommand

    override def receiveRecover: Receive = aggregate.handleEvent orElse receiveAkkaMessages

    def receiveAkkaMessages: Receive = {
        case SnapshotOffer(_, payload: Any) =>
            aggregate.handleEvent(new Snapshot(payload))
        case RecoveryCompleted =>
            log.info(s"Aggregate recovery completed")
    }
}
