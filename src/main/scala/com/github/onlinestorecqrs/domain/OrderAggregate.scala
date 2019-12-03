package com.github.onlinestorecqrs.domain

import com.github.onlinestorecqrs.domain.DomainModel.Order
import DomainApi.{CreateOrderCommand, Get, GetEvent, OrderCreatedEvent}
import com.github.onlinestorecqrs.framework.api.{Aggregate, AggregateLogger, EventManager, Snapshot}
import javax.inject.Inject

class OrderAggregate @Inject()(
    eventManager: EventManager,
    logger: AggregateLogger) extends Aggregate {

    private var order: Order = null

    override def handleCommand: CommandHandler = {
        case CreateOrderCommand(orderId, userId, items) =>
            logger.info("Order requested to be created")
            eventManager.persist(OrderCreatedEvent(orderId, userId, items)) { event =>
                logger.info(s"Order has just been created: $orderId")
                eventManager.notify(event)
            }
        case Get(orderId) =>
            logger.info(s"Order $orderId requested to be retrieved")
            eventManager.notify(new GetEvent(this.order))
    }

    override def handleEvent: EventHandler = {
        case OrderCreatedEvent(orderId, userId, items) =>
            logger.info(s"Order created event: $orderId")
            this.order = new Order(orderId, userId, items)

        case event@GetEvent(_) =>
            logger.info(s"Retrieved ${event}")

        case Snapshot(payload: Order) =>
            this.order = payload
    }

}
