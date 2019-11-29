package com.github.onlinestorecqrs.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, path, pathPrefix, post, _}
import com.github.onlinestorecqrs.api.Api.{ItemDTO, OrderDTO}
import com.github.onlinestorecqrs.domain.DomainApi.{Get, GetEvent, OrderCreatedEvent}
import com.github.onlinestorecqrs.framework.api.CommandGateway
import com.github.onlinestorecqrs.http.JsonFormats._
import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._

/**
  * POST /order { items: [] }               // Create order
  * POST /order/$id/items { items: [] }     // Add item to existing order
  * GET  /order/$id                         // Get order by id
  *
  */
@Singleton
class HttpRoutes @Inject()(
    actorSystem: ActorSystem,
    commandGateway: CommandGateway
) {

    def getRoutes() = {

        pathPrefix("order") {
            concat(
                pathEnd {
                    post {
                        postOrder()
                    }
                },

                pathPrefix(Segment / "items") { id =>
                    pathEnd {
                        post {
                            entity(as[OrderDTO]) { order =>
                                complete(new ItemDTO("1", "23", 10))
                            }
                        }
                    }
                },

                get {
                    path(Segment) { id =>
                        pathEnd {
                            getOrder(id)
                        }
                    }
                }
            )
        }
    }

    private def getOrder(id: String) = {
        val command = new Get(id)

        val future = commandGateway.send("Order", command)

        onComplete(future.mapTo[GetEvent]) { evtTry =>
            if (evtTry.isSuccess) {
                complete(Mapper.mapToDTO(evtTry.get))
            } else {
                failWith(evtTry.failed.get)
            }
        }
    }

    private def postOrder() = {
        entity(as[OrderDTO]) { order =>

            //1. Creates the command
            val command = Mapper.mapToCommand(order)

            //2. Sends the command to the shard region actor
            val future = commandGateway.send("Order", command)

            //3. Register a onComplete with the returned future
            onComplete(future.mapTo[OrderCreatedEvent]) { evtTry =>
                if (evtTry.isSuccess) {
                    complete(Mapper.mapToDTO(evtTry.get))
                } else {
                    failWith(evtTry.failed.get)
                }
            }
        }
    }
}
