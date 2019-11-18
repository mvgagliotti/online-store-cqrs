package com.github.onlinestorecqrs.framework

trait Aggregate[R] {
    def handleCommand: PartialFunction[Any, Unit]

    def handleEvent: PartialFunction[Any, Unit]

    def aggregateRoot(): R
}
