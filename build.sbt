ThisBuild / organization := "ba.sake"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.4"

name := "sbt-squery"

enablePlugins(SbtPlugin)

Compile / scalacOptions += "-Wunused:all"

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  s"-Dplugin.version=${version.value}"
)
scriptedBufferLog := false

homepage := Some(uri("https://github.com/sake92/sbt-squery"))
licenses := Seq("Apache-2.0" -> uri("https://www.apache.org/licenses/LICENSE-2.0"))
developers := List(
  Developer(
    id = "sake92",
    name = "Sakib Hadžiavdić",
    email = "sake92@sake.ba",
    url = uri("https://sake.ba")
  )
)
scmInfo := Some(
  ScmInfo(
    browseUrl = uri("https://github.com/sake92/sbt-squery"),
    connection = "scm:git:git@github.com:sake92/sbt-squery.git"
  )
)
