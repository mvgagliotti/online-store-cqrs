package com.github.onlinestorecqrs.framework

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}

/**
  * Aggregate Persistent Actor
  *
  * @param aggregateBuilder
  * @tparam R
  * @tparam T
  */
class AggregatePersistentActor[T <: Aggregate[R], R](aggregateBuilder: AggregateBuilder[T, R]) extends PersistentActor
    with ActorLogging {

    val eventManagerAdapter = new EventManager {

        override def persist[A](event: A)(handler: A => Unit): Unit = {
            AggregatePersistentActor.super.persist(event)(handler)
        }

        override def notify[A](event: A): Unit = {
            aggregate.handleEvent(event)
            AggregatePersistentActor.super.sender() ! event
        }
    }

    val aggregate: T = aggregateBuilder.build(eventManagerAdapter)

    override def persistenceId: String = self.path.name.split("/")(self.path.name.split("/").length - 1)

    override def receiveCommand: Receive = aggregate.handleCommand

    override def receiveRecover: Receive = aggregate.handleEvent orElse receiveAkkaMessages

    def receiveAkkaMessages: Receive = {
        case SnapshotOffer(_, payload: R) =>
            aggregate.handleEvent(new Snapshot(payload))
        case RecoveryCompleted =>
            log.info(s"Aggregate recovery completed: ${aggregate.aggregateRoot()}")
    }
}
