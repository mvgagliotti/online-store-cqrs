lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.1.10"
lazy val akkaCassandraPersistence = "0.100"
lazy val scalaTestVersion = "3.0.8"

lazy val root = (project in file(".")).
    settings(
        name := "online-store-command",
        version := "2.12.8_1.0",
        scalaVersion := "2.12.8",
        mainClass in Compile := Some("com.github.onlinestorecqrs.Main")
    )

libraryDependencies ++= Seq(
    /////////////////////
    /// Main dependencies
    /////////////////////

    //Akka actor
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,

    //Akka persistence + Cassandra persistence
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % akkaCassandraPersistence,

    //Akka remote and sharding
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,

    //Akka HTTP
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    //Guice
    "com.google.inject" %% "guice" % "4.2.2",

    /////////////////////
    /// Test dependencies
    /////////////////////

    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % akkaCassandraPersistence % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
}
