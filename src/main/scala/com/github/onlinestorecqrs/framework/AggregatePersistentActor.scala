package com.github.onlinestorecqrs.framework

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.github.onlinestorecqrs.framework.api.{Aggregate, Snapshot}
import com.github.onlinestorecqrs.framework.shard.AggregateFactory

/**
  * Aggregate Persistent Actor
  *
  * @param aggregateFactory
  * @tparam T
  */
class AggregatePersistentActor[T <: Aggregate](aggregateFactory: AggregateFactory[T]) extends PersistentActor
    with ActorLogging {

    val aggregate: T = aggregateFactory.build(List(
        new AkkaEventManagerAdapter[T](this),
        new AkkaLoggerAdapter(this))
    )

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
