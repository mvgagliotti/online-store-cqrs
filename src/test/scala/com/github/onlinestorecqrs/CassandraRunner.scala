package com.github.onlinestorecqrs

/**
  * The idea of this runner is to start Cassandra before all tests execute and shut it down after all have been run
  *
  * It is configured as gradle task on build.gradle.kts
  *
  */
object CassandraRunner extends App {
    if (args(0) == "start") {
        //TODO: finish
        println("Hey, starting it!")
    } else if (args(0) == "stop") {
        //TODO: finish
        println("Hey, stopping it!")
    } else throw new IllegalArgumentException("Wrong arguments")
}
