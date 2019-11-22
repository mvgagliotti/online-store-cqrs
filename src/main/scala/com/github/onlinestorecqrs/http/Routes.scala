package com.github.onlinestorecqrs.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, path, pathPrefix, post, _}
import com.github.onlinestorecqrs.api.Api.{ItemDTO, OrderDTO}
import com.github.onlinestorecqrs.domain.DomainApi.OrderCreatedEvent
import com.github.onlinestorecqrs.http.JsonFormats._

import scala.concurrent.duration._

/**
  * POST /order { items: [] }               // Create order
  * POST /order/$id/items { items: [] }     // Add item to existing order
  * GET  /order/$id                         // Get order by id
  *
  */
object Routes {

    def getRoutes(implicit actorSystem: ActorSystem,
                  shardRegion: ActorRef) = {

        pathPrefix("order") {
            concat(
                pathEnd {
                    postOrder(shardRegion)
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
                            complete(new OrderDTO(Some(s"xpto_${id}"), List()))
                        }
                    }
                }
            )
        }
    }

    private def postOrder(shardRegion: ActorRef) = {
        post {
            import akka.pattern.ask
            import akka.util.Timeout

            entity(as[OrderDTO]) { order =>
                implicit val timeout = Timeout(2 seconds)

                //1. Creates the command
                val command = Mapper.mapToCommand(order)

                //2. Sends the command to the shard region actor
                val future = shardRegion ? command

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
}
