import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    scala
    application
}

repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    jcenter()
}

dependencies {
    /////////////////////
    /// Main dependencies
    /////////////////////

    //Akka actor
    implementation("com.typesafe.akka:akka-actor_2.12:2.5.23")

    //Akka persistence + Cassandra persistence
    implementation("com.typesafe.akka:akka-persistence_2.12:2.5.23")
    implementation("com.typesafe.akka:akka-persistence-cassandra_2.12:0.100")

    //Akka remote and sharding
    implementation("com.typesafe.akka:akka-remote_2.12:2.5.23")
    implementation("com.typesafe.akka:akka-cluster_2.12:2.5.23")
    implementation("com.typesafe.akka:akka-cluster-sharding_2.12:2.5.23")
    implementation("com.typesafe.akka:akka-cluster-tools_2.12:2.5.23")

    //Akka HTTP
    implementation("com.typesafe.akka:akka-http_2.12:10.1.10")
    implementation("com.typesafe.akka:akka-http-spray-json_2.12:10.1.10")
    implementation("com.typesafe.akka:akka-stream_2.12:2.5.23")

    /////////////////////
    /// Test dependencies
    /////////////////////
    testImplementation("com.typesafe.akka:akka-actor-testkit-typed_2.12:2.5.23")
    testImplementation("org.scalatest:scalatest:3.0.8")
    testImplementation("com.typesafe.akka:akka-persistence-cassandra-launcher_2.12:0.100")
    testImplementation("com.typesafe.akka:akka-http-testkit_2.12:10.1.10")
}

tasks {
    shadowJar {
        archiveFileName.set("online-store.jar")
        destinationDirectory.set(file("./target"))

        val newTransformer = AppendingTransformer()
        newTransformer.resource = "reference.conf"
        transformers.add(newTransformer)
    }
}

application {
    mainClassName = "com.github.onlinestorecqrs.Main"
}
