package com.github.onlinestorecqrs.domain

import com.github.onlinestorecqrs.domain.DomainModel.{Item, Order}

/**
  * Domain commands % events:
  *
  * --Create an Order with items
  * --Create a Payment for an Order
  * --Retrieve an Order by parameters
  * --Refund Order or any Order Item
  */
object DomainApi {

    //commands
    trait OrderCommand {
        val orderId: String
    }

    case class CreateOrderCommand(orderId: String, userId: String, items: List[Item]) extends OrderCommand

    case class AddItemsCommand(orderId: String, items: List[Item]) extends OrderCommand

    case class IncreaseItemAmount(orderId: String, item: Item, amountToAdd: Int) extends OrderCommand

    case class PayOrderCommand(orderId: String, value: BigDecimal) extends OrderCommand

    case class RefundOrderCommand(orderId: String, value: BigDecimal) extends OrderCommand

    case class Get(orderId: String) extends OrderCommand


    //events
    case class OrderCreatedEvent(orderId: String, userId: String, items: List[Item])

    case class ItemsAddedEvent(orderId: String, items: List[Item])

    case class ItemAmountIncreasedEvent(orderId: String, item: Item, amountToAdd: Int)

    case class OrderPayedEvent(orderId: String, value: BigDecimal)

    case class OrderRefundedEvent(orderId: String, value: BigDecimal)

    case class GetEvent(order: Order)

}
