import Dependencies._

Project.inConfig(Test)(baseAssemblySettings)

assemblyJarName in (Test, assembly) := s"${name.value}-test-${version.value}.jar"

lazy val Benchmark = config("bench") extend Test

/**  This allows running ScalaMeter benchmarks in separate sbt configuration.
  *  It means, that when you want run your benchmarks you should type `bench:test` in sbt console.
  */
lazy val basic = Project(
  "root-build",
  file("."),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "DB_Project1",
    organization := "",
    scalaVersion := "2.11.1",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    publishArtifact := false,
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaMeter % "bench",
      scalaMeterCore % "bench"
    ),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    //parallelExecution in Benchmark := false,
    logBuffered := false
  )
) configs(
  Benchmark
  ) settings(
  inConfig(Benchmark)(Defaults.testSettings): _*
  )
