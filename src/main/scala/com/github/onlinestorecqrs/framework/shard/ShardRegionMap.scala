package com.github.onlinestorecqrs.framework.shard

import akka.actor.ActorRef

/**
  * A wrapper for Map[String, ActorRef]
  *
  * @param map the source map
  */
class ShardRegionMap(map: Map[String, ActorRef]) {

    def get(key: String) = map.get(key)

}
