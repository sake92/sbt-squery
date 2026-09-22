package ba.sake.sbt.squery

import sbt.*
import sbt.Keys.*

import java.net.URLClassLoader

object SqueryPlugin extends AutoPlugin:

  object autoImport:
    val squeryJdbcUrl = settingKey[String]("JDBC URL used for Squery source generation")
    val squeryJdbcDeps = settingKey[Seq[ModuleID]]("JDBC drivers used for Squery source generation")
    val squerySchemaMappings = settingKey[Seq[(String, String)]]("Mappings from database schemas to Scala packages")

    val squeryColNameIdentifierMapper = settingKey[String]("Column identifier mapper: camelcase or noop")
    val squeryTypeNameMapper = settingKey[String]("Type name mapper: camelcase or noop")
    val squeryRowTypeSuffix = settingKey[String]("Suffix for generated row types")
    val squeryDaoTypeSuffix = settingKey[String]("Suffix for generated DAO types")
    val squeryTypeMappingRules = settingKey[Seq[String]](
      "Ordered column-name-regex|declared-type-regex|Scala-type mapping rules"
    )
    val squeryIncludeTables = settingKey[Seq[String]]("Regexes of schema.table names to generate")
    val squeryExcludeTables = settingKey[Seq[String]]("Regexes of schema.table names to exclude")

    val squeryTargetDir = settingKey[File]("Directory where Squery writes generated sources")
    val squeryVersion = settingKey[String]("Squery CLI version used for source generation")
    val squeryClasspath = taskKey[Classpath]("Classpath used to run the Squery generator")
    val squeryGenerate = taskKey[Unit]("Generate Squery models and DAOs from the database")

  import autoImport.*

  private val SqueryGenerator = config("squeryGenerator").hide

  override def requires: Plugins = plugins.JvmPlugin

  override def projectConfigurations: Seq[Configuration] = Seq(SqueryGenerator)

  override def projectSettings: Seq[Setting[?]] = inConfig(SqueryGenerator)(Defaults.configSettings) ++ Seq(
    squeryColNameIdentifierMapper := "camelcase",
    squeryTypeNameMapper := "camelcase",
    squeryRowTypeSuffix := "Row",
    squeryDaoTypeSuffix := "Dao",
    squeryTypeMappingRules := Seq.empty,
    squeryIncludeTables := Seq.empty,
    squeryExcludeTables := Seq.empty,
    squeryTargetDir := (Compile / scalaSource).value,
    squeryVersion := "0.12.0",
    libraryDependencies ++= generatorDependencies(
      squeryVersion.value,
      squeryJdbcDeps.?.value.getOrElse(Seq.empty)
    ),
    squeryClasspath := Def.uncached((SqueryGenerator / fullClasspath).value),
    squeryGenerate := Def.uncached {
      val converter = fileConverter.value
      generate(
        classpath = squeryClasspath.value.map(entry => converter.toPath(entry.data).toFile),
        jdbcUrl = squeryJdbcUrl.value,
        targetDir = squeryTargetDir.value,
        schemaMappings = squerySchemaMappings.value,
        colNameIdentifierMapper = squeryColNameIdentifierMapper.value,
        typeNameMapper = squeryTypeNameMapper.value,
        rowTypeSuffix = squeryRowTypeSuffix.value,
        daoTypeSuffix = squeryDaoTypeSuffix.value,
        typeMappingRules = squeryTypeMappingRules.value,
        includeTables = squeryIncludeTables.value,
        excludeTables = squeryExcludeTables.value,
        log = streams.value.log
      )
    }
  )

  private def generatorDependencies(version: String, jdbcDependencies: Seq[ModuleID]): Seq[ModuleID] =
    (jdbcDependencies :+ ("ba.sake" % "squery-cli_2.13" % version)).map(_ % SqueryGenerator.name)

  private def generate(
      classpath: Seq[File],
      jdbcUrl: String,
      targetDir: File,
      schemaMappings: Seq[(String, String)],
      colNameIdentifierMapper: String,
      typeNameMapper: String,
      rowTypeSuffix: String,
      daoTypeSuffix: String,
      typeMappingRules: Seq[String],
      includeTables: Seq[String],
      excludeTables: Seq[String],
      log: Logger
  ): Unit =
    val tableIncludes = if includeTables.isEmpty then Seq(".*") else includeTables
    val args =
      Seq(
        "--jdbcUrl",
        jdbcUrl,
        "--baseFolder",
        targetDir.getAbsolutePath,
        "--colNameIdentifierMapper",
        colNameIdentifierMapper,
        "--typeNameMapper",
        typeNameMapper,
        "--rowTypeSuffix",
        rowTypeSuffix,
        "--daoTypeSuffix",
        daoTypeSuffix
      ) ++
        repeatedArgs(
          "--schemaMappings",
          schemaMappings.map { case (schema, packageName) => s"$schema:$packageName" }
        ) ++
        repeatedArgs("--typeMappingRule", typeMappingRules) ++
        repeatedArgs("--includeTables", tableIncludes) ++
        repeatedArgs("--excludeTables", excludeTables)

    IO.createDirectory(targetDir)
    log.info("Starting to generate Squery sources...")
    runGenerator(classpath, args.toArray)
    log.info("Finished generating Squery sources")

  private def repeatedArgs(name: String, values: Seq[String]): Seq[String] =
    values.flatMap(value => Seq(name, value))

  private def runGenerator(classpath: Seq[File], args: Array[String]): Unit =
    val previousClassLoader = Thread.currentThread().getContextClassLoader
    val classLoader = new URLClassLoader(
      classpath.map(_.toURI.toURL).toArray,
      ClassLoader.getPlatformClassLoader
    )
    try
      Thread.currentThread().setContextClassLoader(classLoader)
      classLoader
        .loadClass("ba.sake.squery.cli.SqueryMain")
        .getMethod("main", classOf[Array[String]])
        .invoke(null, args)
    finally
      Thread.currentThread().setContextClassLoader(previousClassLoader)
      classLoader.close()
