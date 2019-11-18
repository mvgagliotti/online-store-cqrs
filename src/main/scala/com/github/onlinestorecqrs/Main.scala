package com.github.onlinestorecqrs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.onlinestorecqrs.http.Routes
import com.github.onlinestorecqrs.shard.OrderShardRegion
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
  * TODO:
  *
  * 1. Uber jar (OK)
  * 2. Run Cassandra via Docker (OK)
  * 3. Configure cassandra plugin (OK)
  * 4. Test a persistent actor (OK)
  * 5. Configure a serializer for cassandra persistence
  * 6. Test sharding (OK)
  * 7. Best practices: loading, updating (~OK)
  * 8. Add Akka HTTP (OK)
  * 9  Add token auth
  *
  * 9. Implement the basic endpoints:
  *     Create a Store
  *     Update a Store information
  *     Retrieve a Store by parameters
  *     Create an Order with items
  *     Create a Payment for an Order
  *     Retrieve an Order by parameters
  *     Refund Order or any Order Item
  *
  * 10. Liveness and Readiness probes: https://doc.akka.io/docs/akka-management/current/healthchecks.html
  */
object Main extends App {

    //getting cluster node port by command line args, or falling back to 2551
    val nodePort: String = if (args.nonEmpty) args(0) else "2551"

    println(s"Starting Online Store node at port $nodePort")

    //creating actor system with all the persistence and cluster configuration
    val config = ConfigFactory.parseString(
        s"""
           |akka.remote.artery.canonical.port = $nodePort
        """.stripMargin).withFallback(ConfigFactory.load("app.conf"))

    //creating the actor system along with shard region actor
    implicit val actorSystem = ActorSystem("OnlineStore", config)
    val shardRegion = OrderShardRegion.setupClusterSharding(actorSystem)

    //defining some boilerplate implicits
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatcher // needed for the future flatMap/onComplete in the end

    //binding routes and starting the server
    val routes = Routes.getRoutes(actorSystem, shardRegion)
    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => actorSystem.terminate()) // and shutdown when done

}
