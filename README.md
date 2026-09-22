# sbt-squery

sbt 2 plugin for [Squery](https://github.com/sake92/squery).

It generates database models and DAOs and incrementally refactors them with
[Regenesca](https://github.com/sake92/regenesca).

Add the plugin to `project/plugins.sbt`:

```scala
addSbtPlugin("ba.sake" % "sbt-squery" % "0.1.0")
```

Enable and configure it in `build.sbt`:

```scala
import ba.sake.sbt.squery.SqueryPlugin.autoImport.*

enablePlugins(ba.sake.sbt.squery.SqueryPlugin)

squeryJdbcUrl := "jdbc:h2:..."
squeryJdbcDeps := Seq("com.h2database" % "h2" % "2.3.232")

// Mappings from database schemas to Scala packages
squerySchemaMappings := Seq("PUBLIC" -> "com.myapp.public")

// Optional configuration
// squeryColNameIdentifierMapper := "camelcase" // or "noop"
// squeryTypeNameMapper := "camelcase" // or "noop"
// squeryRowTypeSuffix := "Row"
// squeryDaoTypeSuffix := "Dao"
// squeryTypeMappingRules := Seq(".*_id|UUID|java.util.UUID")
// squeryIncludeTables := Seq("PUBLIC\\.(actor|address)")
// squeryExcludeTables := Seq("PUBLIC\\.address") // exclusions win
// squeryTargetDir := (Compile / scalaSource).value
// squeryVersion := "0.12.0"
```

Generate sources with:

```shell
sbt squeryGenerate
```

By default, the plugin uses Squery `0.12.0` and writes sources to
`Compile / scalaSource` (`src/main/scala`). The Squery CLI does not bundle JDBC
drivers, so add the driver for your database to `squeryJdbcDeps`.

`squeryTypeMappingRules`, `squeryIncludeTables`, and `squeryExcludeTables` are
ordered, repeatable CLI options. Table patterns match fully qualified
`schema.table` names, and exclusions take precedence.
