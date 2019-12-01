package com.github.onlinestorecqrs.framework

import com.github.onlinestorecqrs.framework.api.{Aggregate, EventManager}

class AkkaEventManagerAdapter[T <: Aggregate](persistentActor: AggregatePersistentActor[T]) extends EventManager {

    override def persist[A](event: A)(handler: A => Unit): Unit = {
        persistentActor.persist(event)(handler)
    }

    override def notify[A](event: A): Unit = {
        persistentActor.aggregate.handleEvent(event)
        persistentActor.sender() ! event
    }
}
