ThisBuild / organization := "ba.sake"

name := "sbt-squery"

enablePlugins(SbtPlugin)

description := "sbt 2 plugin for Squery"
pluginCrossBuild / sbtVersion := "2.0.0"
scriptedSbt := "2.0.9"

Compile / scalacOptions += "-Wunused:all"

scriptedLaunchOpts := Seq(
  "-Xmx1G",
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
versionScheme := Some("semver-spec")
