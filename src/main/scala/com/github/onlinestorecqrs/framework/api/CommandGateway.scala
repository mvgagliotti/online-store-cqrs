package com.github.onlinestorecqrs.framework.api

import scala.concurrent.Future

trait CommandGateway {
    def send[T](aggregateName: String, msg: Any): Future[Any]
}
