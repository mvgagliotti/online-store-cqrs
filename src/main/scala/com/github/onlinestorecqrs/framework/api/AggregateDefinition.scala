package com.github.onlinestorecqrs.framework.api

import com.github.onlinestorecqrs.framework.api.AggregateDefinition.{IdExtractor, ShardIdExtractor}

object AggregateDefinition {
    type IdExtractor = PartialFunction[Any, (String, Any)]
    type ShardIdExtractor = Any => String
}

case class AggregateDefinition[T](
    name: String,
    aggregateClass: Class[Aggregate[T]],
    idExtractor: IdExtractor,
    shardIdExtractor: ShardIdExtractor
)
