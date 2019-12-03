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
    /// Versions
    /////////////////////
    val scalaVersion = "2.12"
    val scalaTestVersion = "3.0.8"
    val akkaVersion = "2.5.23"
    val akkaHttpVersion = "10.1.10"
    val akkaPersistenceCassandraVersion = "0.100"

    /////////////////////
    /// Main dependencies
    /////////////////////
    compile("org.scala-lang:scala-library:2.12.8")

    //Akka actor
    implementation("com.typesafe.akka:akka-actor_$scalaVersion:$akkaVersion")

    //Akka persistence + Cassandra persistence
    implementation("com.typesafe.akka:akka-persistence_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-persistence-cassandra_$scalaVersion:$akkaPersistenceCassandraVersion")

    //Akka remote and sharding
    implementation("com.typesafe.akka:akka-remote_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-cluster_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-cluster-sharding_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-cluster-tools_$scalaVersion:$akkaVersion")

    //Akka HTTP
    implementation("com.typesafe.akka:akka-http_$scalaVersion:$akkaHttpVersion")
    implementation("com.typesafe.akka:akka-http-spray-json_$scalaVersion:$akkaHttpVersion")
    implementation("com.typesafe.akka:akka-stream_$scalaVersion:$akkaVersion")

    //Guice
    implementation("com.google.inject:guice:4.2.2")

    /////////////////////
    /// Test dependencies
    /////////////////////
    testImplementation("com.typesafe.akka:akka-testkit_$scalaVersion:$akkaVersion")
    testImplementation("org.scalatest:scalatest_$scalaVersion:$scalaTestVersion")
    testImplementation("com.typesafe.akka:akka-persistence-cassandra-launcher_$scalaVersion:$akkaPersistenceCassandraVersion")
    testImplementation("com.typesafe.akka:akka-http-testkit_$scalaVersion:$akkaHttpVersion")
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

//these tasks are necessary to run the tests

val startCassandra = task("startCassandra", JavaExec::class) {
    main = "com.github.onlinestorecqrs.CassandraRunner"
    args = listOf("start")
    classpath = sourceSets["test"].runtimeClasspath
}

val spec = task("spec", JavaExec::class) {
    main = "org.scalatest.tools.Runner"
    args = listOf("-R", "build/classes/scala/test", "-o")
    classpath = sourceSets["test"].runtimeClasspath
}

spec.dependsOn(startCassandra)

task("stopCassandra", JavaExec::class) {
    main = "com.github.onlinestorecqrs.CassandraRunner"
    args = listOf("stop")
    classpath = sourceSets["test"].runtimeClasspath
}.shouldRunAfter(spec)


application {
    mainClassName = "com.github.onlinestorecqrs.Main"
}
