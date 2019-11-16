package com.github.onlinestorecqrs.domain.persistence

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import com.github.onlinestorecqrs.domain.DomainModel._
import com.github.onlinestorecqrs.domain.persistence.OrderActor.{CreateOrderCommand, OrderCommand, OrderCreatedEvent}

/**
  * Domain commands % events:
  *
  * --Create an Order with items
  * --Create a Payment for an Order
  * --Retrieve an Order by parameters
  * --Refund Order or any Order Item
  */
object OrderActor {

    //commands
    trait OrderCommand {
        val orderId: String
    }

    case class CreateOrderCommand(orderId: String, userId: String, items: List[Item]) extends OrderCommand

    case class AddItemsCommand(orderId: String, items: List[Item]) extends OrderCommand

    case class IncreaseItemAmount(orderId: String, item: Item, amountToAdd: Int) extends OrderCommand

    case class PayOrderCommand(orderId: String, value: BigDecimal) extends OrderCommand

    case class RefundOrderCommand(orderId: String, value: BigDecimal) extends OrderCommand

    //events
    case class OrderCreatedEvent(orderId: String, userId: String, items: List[Item])

    case class ItemsAddedEvent(orderId: String, items: List[Item])

    case class ItemAmountIncreasedEvent(orderId: String, item: Item, amountToAdd: Int)

    case class OrderPayedEvent(orderId: String, value: BigDecimal)

    case class OrderRefundedEvent(orderId: String, value: BigDecimal)

}

/**
  * Persistent actor
  *
  */
class OrderActor extends PersistentActor with ActorLogging {

    private var order: Option[Order] = None

    override def persistenceId: String = order match{
        case None => self.path.name.split("/")(self.path.name.split("/").length - 1)
        case Some(orderValue) => orderValue.id
    }

    override def receiveRecover: Receive = {
        case OrderCreatedEvent(orderId, userId, items) => this.order = Some(Order(orderId, userId, items))
    }

    override def receiveCommand: Receive = {

        case CreateOrderCommand(orderId, userId, items) =>
            log.info("A order creation command has arrived")
            persist(OrderCreatedEvent(orderId, userId, items)) { event =>
                this.order = Some(Order(event.orderId, event.userId, event.items))
                sender() ! event
                log.info(s"Order ${event.orderId} has been persisted")
            }
    }
}
