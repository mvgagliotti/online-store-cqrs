package com.github.onlinestorecqrs.framework.api

trait EventManager {
    def persist[A](event: A)(handler: A => Unit): Unit

    def notify[A](event: A)
}
