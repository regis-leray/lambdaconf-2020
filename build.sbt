name := "lambdaconf2020"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= List(
  "com.typesafe" % "config" % "1.3.2",

  "co.fs2" %% "fs2-core" % "2.4.4",
  "co.fs2" %% "fs2-io" % "2.4.4",

  "dev.zio" %% "zio" % "1.0.1",
  "dev.zio" %% "zio-streams" % "1.0.1"
)

//scalacOptions ++= Seq("-language:higherKinds")
scalacOptions += "-Xfatal-warnings"
