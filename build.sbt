import play.sbt.PlayScala
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._

lazy val Versions = new {
  val util = "0.27.8"
}

def unitFilter(name: String): Boolean = (name endsWith "Test") || (name endsWith "Spec")  // && !boxFilter(name)

lazy val commonSettings =
  Seq(
  scalaVersion := "2.11.8",
    // next properties set required for sbt-assembly plugin,
    // whe it finds two classes with same name in different JARs it does not know what to do
    // we're defining merge strategy for problematic classes (mostly it's spark deps)
    assemblyMergeStrategy in assembly := {
      case PathList("io", "netty", xs@_*) => MergeStrategy.last
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
      case PathList("javax", "xml", xs@_*) => MergeStrategy.last
      case PathList("javax", "activation", xs@_*) => MergeStrategy.last
      case PathList("org", "apache", xs@_*) => MergeStrategy.last
      case PathList("org", "slf4j", xs@_*) => MergeStrategy.first
      case PathList("org", "joda", xs@_*) => MergeStrategy.last
      case PathList("com", "google", xs@_*) => MergeStrategy.last
      case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
      case PathList("com", "codahale", xs@_*) => MergeStrategy.last
      case PathList("com", "yammer", xs@_*) => MergeStrategy.last
      case "about.html" => MergeStrategy.rename
      case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
      case "META-INF/mailcap" => MergeStrategy.last
      case "META-INF/mimetypes.default" => MergeStrategy.last
      case "META-INF/native/libnetty-transport-native-epoll.so" => MergeStrategy.last
      case "plugin.properties" => MergeStrategy.last
      case "log4j.properties" => MergeStrategy.last
      case "application.conf" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
  resolvers ++= Seq(
    Resolver.bintrayRepo("outworkers", "oss-releases"),
    "splunk" at "http://splunk.artifactoryonline.com/splunk/ext-releases-local"
  ),
  testOptions in Test := Seq(Tests.Filter(unitFilter)),
  coverageExcludedPackages := ".*Routes.*;.*ReverseRoutes.*;.*javascript.*",
    scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-language:reflectiveCalls",
    "-language:experimental.macros",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:postfixOps",
    "-deprecation", // warning and location for usages of deprecated APIs
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-Xlint", // recommended additional warnings
    "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    //"-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
    "-Ywarn-dead-code", // Warn when dead code is identified
    "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
    "-Ywarn-unused-import", //  Warn when imports are unused (don't want IntelliJ to do it automatically)
    "-Ywarn-numeric-widen" // Warn when numerics are widened
  )
)

lazy val api = (project in file("."))
  .enablePlugins(BuildInfoPlugin, PlayScala)
  .settings(commonSettings,
    name := "message-queue-poc",
    scalaVersion := "2.11.8",
    buildInfoPackage := "controllers",
    fork in run := true,
    buildInfoKeys ++= Seq[BuildInfoKey](
      resolvers,
      libraryDependencies,
      BuildInfoKey.action("gitRevision") {
        ("git rev-parse --short HEAD" !!).trim
      }
    ),
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,

    // no javadoc for BuildInfo.scala
    sources in(Compile, doc) <<= sources in(Compile, doc) map {
      _.filterNot(_.getName endsWith ".scala")
    },

    assemblyJarName in assembly := "ons-message-queue.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "io.netty.versions.properties", xs@_ *) => MergeStrategy.last
      case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first // ES shades Joda
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    libraryDependencies ++= Seq(
      filters,
      "com.newmotion" %% "akka-rabbitmq" % "5.0.0",
      "org.webjars" %% "webjars-play" % "2.5.0-3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "com.outworkers" %% "util-play" % Versions.util,
      "org.scalamock"  %% "scalamock" % "4.1.0" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M1" % Test,
      "com.github.tomakehurst" % "wiremock" % "1.58" % Test,
      "org.scalatest" %% "scalatest" % "3.0.0" % Test,
      "com.google.guava" % "guava" % "18.0"
    ),

    dependencyOverrides += "com.google.guava" % "guava" % "18.0"
  )
