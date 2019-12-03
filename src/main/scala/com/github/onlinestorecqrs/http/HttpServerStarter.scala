package com.github.onlinestorecqrs.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import scala.io.StdIn

@Singleton
class HttpServerStarter @Inject()(
    actorSystem: ActorSystem,
    routes: HttpRoutes
) {

    private var bindingFuture: Future[Http.ServerBinding] = null

    def start(runUntilEnterIsPressed: Boolean = true): Unit = {

        //defining some boilerplate implicits
        implicit val system = actorSystem
        implicit val materializer = ActorMaterializer()

        bindingFuture = Http().bindAndHandle(routes.getRoutes(), "localhost", 8080)

        if (runUntilEnterIsPressed) {
            println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
            StdIn.readLine() // let it run until user presses return
            stop()
        }
    }

    def stop(): Unit = {
        implicit val executionContext = actorSystem.dispatcher // needed for the future flatMap/onComplete in the end
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => actorSystem.terminate()) // and shutdown when done
    }

}
