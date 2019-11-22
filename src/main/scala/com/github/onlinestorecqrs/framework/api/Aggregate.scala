package com.github.onlinestorecqrs.framework.api

trait Aggregate {
    type CommandHandler = PartialFunction[Any, Unit]
    type EventHandler = PartialFunction[Any, Unit]

    def handleCommand: CommandHandler

    def handleEvent: EventHandler
}
