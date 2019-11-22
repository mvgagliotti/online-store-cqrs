package com.github.onlinestorecqrs.http

import com.github.onlinestorecqrs.api.Api.{ItemDTO, OrderDTO}
import com.github.onlinestorecqrs.domain.DomainModel.{Item, Order}
import spray.json.DefaultJsonProtocol.{jsonFormat2, jsonFormat3, jsonFormat4}
import spray.json.DefaultJsonProtocol._

object JsonFormats {

    // formats for unmarshalling and marshalling
    implicit val itemFormat = jsonFormat4(Item)
    implicit val orderFormat = jsonFormat3(Order)
    implicit val itemDTOFormat = jsonFormat3(ItemDTO)
    implicit val orderDTOFormat = jsonFormat2(OrderDTO)
}
