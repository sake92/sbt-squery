import ba.sake.sbt.squery.SqueryPlugin.autoImport.*

ThisBuild / scalaVersion := "3.9.0"

enablePlugins(ba.sake.sbt.squery.SqueryPlugin)

libraryDependencies ++= Seq(
  "ba.sake" %% "squery" % "0.13.0",
  "com.h2database" % "h2" % "2.3.232"
)

squeryJdbcUrl := {
  val schema = (ThisBuild / baseDirectory).value / "schema.sql"
  s"jdbc:h2:mem:squery;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM '${schema.toURI}'"
}
squeryJdbcDeps := Seq("com.h2database" % "h2" % "2.3.232")
squerySchemaMappings := Seq("PUBLIC" -> "example")
squeryIncludeTables := Seq("PUBLIC\\..*")
squeryExcludeTables := Seq("PUBLIC\\.AUDIT_LOG")
