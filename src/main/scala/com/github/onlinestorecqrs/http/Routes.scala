package com.github.onlinestorecqrs.http

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, path, pathPrefix, post, _}
import akka.stream.ActorMaterializer
import com.github.onlinestorecqrs.api.Api.{ItemDTO, OrderDTO}
import com.github.onlinestorecqrs.domain.DomainModel._
import com.github.onlinestorecqrs.domain.persistence.OrderActor.{CreateOrderCommand, OrderCreatedEvent}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * POST /order { items: [] }               // Create order
  * POST /order/$id/items { items: [] }     // Add item to existing order
  * GET  /order/$id                         // Get order by id
  *
  */
object Routes {

    def getRoutes(implicit actorSystem: ActorSystem,
                  shardRegion: ActorRef): server.Route = {

        implicit val materializer = ActorMaterializer()
        implicit val executionContext = actorSystem.dispatcher // needed for the future flatMap/onComplete in the end

        // formats for unmarshalling and marshalling
        implicit val itemFormat = jsonFormat4(Item)
        implicit val orderFormat = jsonFormat3(Order)
        implicit val itemDTOFormat = jsonFormat3(ItemDTO)
        implicit val orderDTOFormat = jsonFormat2(OrderDTO)

        pathPrefix("order") {
            concat(
                pathEnd {
                    post {
                        import akka.pattern.ask
                        import akka.util.Timeout

                        entity(as[OrderDTO]) { order =>
                            implicit val timeout = Timeout(2 seconds)

                            //1. Creates the command
                            val command = CreateOrderCommand(
                                UUID.randomUUID().toString,
                                "user-123",
                                order.items.map { dto =>
                                    Item(id = dto.id, description = dto.description, amount = dto.amount, value = 0)
                                })

                            //2. Sends the command to the shard region actor
                            val future = shardRegion ? command

                            onComplete(future.mapTo[OrderCreatedEvent]) {
                                case Success(event) => complete(OrderDTO(
                                    id = Some(event.orderId),
                                    items = List()
                                ))
                                case Failure(exception) => failWith(exception)
                            }
                        }
                    }
                },
                pathPrefix(Segment / "items") { _ =>
                    pathEnd {
                        post {
                            entity(as[OrderDTO]) { _ =>
                                complete(ItemDTO("1", "23", 10))
                            }
                        }
                    }
                },
                get {
                    path(Segment) { id =>
                        pathEnd {
                            complete(OrderDTO(Some(s"xpto_$id"), List()))
                        }
                    }
                }
            )
        }
    }

}
