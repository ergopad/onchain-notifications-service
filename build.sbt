name := """notifications-service"""
organization := "io.ergopad"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.17"

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerUpdateLatest := true
dockerBaseImage := "openjdk:11"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.2.12"
)

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.21.1"
libraryDependencies += "com.github.tminglei" %% "slick-pg_play-json" % "0.21.1"

// https://mvnrepository.com/artifact/org.ergoplatform/ergo-appkit
libraryDependencies += "org.ergoplatform" %% "ergo-appkit" % "5.0.0"

// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.12.409",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.1"
)

libraryDependencies += ws
