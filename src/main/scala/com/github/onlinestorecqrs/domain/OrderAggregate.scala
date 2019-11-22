package com.github.onlinestorecqrs.domain

import com.github.onlinestorecqrs.domain.DomainModel.Order
import DomainApi.{CreateOrderCommand, Get, OrderCreatedEvent}
import com.github.onlinestorecqrs.framework.api.Aggregate
import com.github.onlinestorecqrs.framework.{AggregateLogger, EventManager, Snapshot}

class OrderAggregate(eventManager: EventManager,
                     logger: AggregateLogger) extends Aggregate[Order] {

    private var order: Order = null

    override def handleCommand: CommandHandler = {
        case CreateOrderCommand(orderId, userId, items) =>
            eventManager.persist(OrderCreatedEvent(orderId, userId, items)) { event =>
                logger.info(s"Order has just been created: $orderId")
                eventManager.notify(event)
            }
        case Get(orderId) =>
            logger.info(s"Order $orderId retrieved")
    }

    override def handleEvent: EventHandler = {
        case OrderCreatedEvent(orderId, userId, items) =>
            logger.info(s"Order created event: $orderId")
            this.order = new Order(orderId, userId, items)
        case Snapshot(payload: Order) =>
            this.order = payload
    }

}
