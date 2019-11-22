package com.github.onlinestorecqrs.framework.module

import com.github.onlinestorecqrs.framework.api.{AggregateDefinition, CommandGateway}
import com.github.onlinestorecqrs.framework.shard.CommandGatewayImpl
import com.google.inject.{AbstractModule, Injector, Provider, Provides}
import javax.inject.{Inject, Singleton}

/**
  * Module to wire components
  */
abstract class CQRSModule extends AbstractModule {

    def configureComponents(): Unit = {}

    def aggregateDefinitions(): List[AggregateDefinition]

    @Provides
    @Singleton
    final def providedDefinitions() = aggregateDefinitions()

    final override def configure(): Unit = {
        bind(classOf[InjectorDelegate]).toProvider(classOf[InjectorProvider])
        bind(classOf[CommandGateway]).to(classOf[CommandGatewayImpl])
        configureComponents()
    }

}

/**
  * Auxiliary provider to allow access of injector inside components
  *
  * @param injector Guice injector
  */
@Singleton
class InjectorProvider @Inject()(
    injector: Injector
) extends Provider[InjectorDelegate] {
    override def get(): InjectorDelegate = {
        new InjectorDelegate(injector)
    }
}
