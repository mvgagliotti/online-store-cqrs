package com.github.onlinestorecqrs.framework.api

import com.google.inject.AbstractModule

class CQRSModule extends AbstractModule {


    def registerAggregate[T, R <: Aggregate[R]](definition: AggregateDefinition[T]): Unit = {
        //TODO
    }
}
