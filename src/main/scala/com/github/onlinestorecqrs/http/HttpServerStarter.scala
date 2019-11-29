package com.github.onlinestorecqrs.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import javax.inject.{Inject, Singleton}

import scala.io.StdIn

@Singleton
class HttpServerStarter @Inject()(
    actorSystem: ActorSystem,
    routes: HttpRoutes
) {
    def start(): Unit = {

        //defining some boilerplate implicits
        implicit val system = actorSystem
        implicit val materializer = ActorMaterializer()
        implicit val executionContext = actorSystem.dispatcher // needed for the future flatMap/onComplete in the end

        val bindingFuture = Http().bindAndHandle(routes.getRoutes(), "localhost", 8080)

        println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
        StdIn.readLine() // let it run until user presses return
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => actorSystem.terminate()) // and shutdown when done
    }

}
