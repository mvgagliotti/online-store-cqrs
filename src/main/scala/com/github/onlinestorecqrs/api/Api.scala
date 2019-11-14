package com.github.onlinestorecqrs.api

object Api {

    case class OrderDTO(id: Option[String] = None, items: List[ItemDTO])

    case class ItemDTO(id: String, description: String, amount: Int)

}
