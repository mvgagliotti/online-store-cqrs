package com.github.onlinestorecqrs.framework

trait EventManager {
    def persist[A](event: A)(handler: A => Unit): Unit

    def notify[A](event: A)
}
