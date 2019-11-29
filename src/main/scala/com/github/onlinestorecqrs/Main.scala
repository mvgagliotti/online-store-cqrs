package com.github.onlinestorecqrs

import com.github.onlinestorecqrs.dimodules.MainModule
import com.github.onlinestorecqrs.http.HttpServerStarter
import com.google.inject.Guice
import org.slf4j.LoggerFactory


object Main extends App {

    val logger = LoggerFactory.getLogger(this.getClass())

    //getting cluster node port by command line args, or falling back to 2551
    val nodePort: Int = if (args.nonEmpty) args(0).toInt else 2551
    logger.info(s"Starting Online Store node at port $nodePort")

    val module = new MainModule(nodePort)
    val injector = Guice.createInjector(module)
    val server = injector.getInstance(classOf[HttpServerStarter])

    server.start()
}
