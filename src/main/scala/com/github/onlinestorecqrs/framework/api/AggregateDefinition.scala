package com.github.onlinestorecqrs.framework.api

import com.github.onlinestorecqrs.framework.api.AggregateDefinition.{IdExtractor, ShardIdExtractor}

object AggregateDefinition {
    type IdExtractor = PartialFunction[Any, (String, Any)]
    type ShardIdExtractor = Any => String
}

case class AggregateDefinition(
    name: String,
    aggregateClass: Class[_ <: Aggregate],
    idExtractor: IdExtractor,
    shardIdExtractor: ShardIdExtractor
)
