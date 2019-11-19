package com.github.onlinestorecqrs.framework

trait Aggregate[R] {

    type CommandHandler = PartialFunction[Any, Unit]
    type EventHandler = PartialFunction[Any, Unit]

    def handleCommand: CommandHandler

    def handleEvent: EventHandler

    def aggregateRoot(): R
}
