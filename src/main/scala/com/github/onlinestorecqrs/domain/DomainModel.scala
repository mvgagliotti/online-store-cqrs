package com.github.onlinestorecqrs.domain

object DomainModel {

    case class Order(id: String, userId: String, items: List[Item]) {
        def total: BigDecimal = {
            0 //TODO: implement
        }
    }

    case class Item(id: String, description: String, value: BigDecimal, amount: Int)

    case class Payment(orderId: String, value: BigDecimal)

    case class Refund(orderId: String, value: BigDecimal)

}
