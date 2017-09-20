name := "TalaTest"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= {
  val akkaHttpV = "10.0.8"
  val scalaTestV = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.github.nscala-time" %% "nscala-time" % "2.16.0"
  )
}

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.4.187"
)
