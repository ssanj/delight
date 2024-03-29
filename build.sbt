lazy val scala212 = "2.12.17"

lazy val scala213 = "2.13.10"

lazy val scala3 = "3.1.0"

lazy val supportedScalaVersions = List(scala212, scala213, scala3)

ThisBuild / scalaVersion := scala212

lazy val scalacOptionsIn212 = 
  Seq(
    "-unchecked",
    "-encoding", "UTF-8",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Xlint:_",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-unused-import",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit"
  )

lazy val scalacOptionsIn213 = 
  Seq(
    "-unchecked",
    "-encoding", "UTF-8",
    "-deprecation",
    "-feature",
    "-Werror",
    "-Xlint:_",
    "-Wdead-code",
    "-Wunused:_"
  )

lazy val scalacOptionsIn3 =
  Seq(
    "-unchecked",
    "-encoding", "UTF-8",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
  )

lazy val scalacSettings = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12))=> scalacOptionsIn212
    case Some((2, 13))=> scalacOptionsIn213
    case Some((3, _))=> scalacOptionsIn3
    case _ => Seq.empty[String]
  }
}

lazy val commonSettings = Seq(
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  Compile / compile / wartremoverErrors ++= Warts.allBut(Wart.Any)
)

lazy val scalaTest = "org.scalatest"  %% "scalatest"   % "3.2.14"

lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.17.0" % Test

lazy val parent = (project in file("."))
  .aggregate(delight, sample)
  .settings(
    commonSettings,
    publish  / skip := true,
    publishLocal  / skip := true
  )

lazy val sample = (project in file("sample"))
  .dependsOn(delight)
  .settings(
    commonSettings,
    scalacOptions ++= scalacSettings.value,    
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(scalaTest % Test),
    publish  / skip := true,
    publishLocal  / skip := true
  )

lazy val delight = (project in file("delight"))
  .settings(
    commonSettings,
    scalacOptions ++= scalacSettings.value,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(scalaTest % Compile, scalaCheck)
)


Global / onChangedBuildSource := ReloadOnSourceChanges

// https://www.scala-sbt.org/1.x/docs/Publishing.html
// Semantic Versioning where all 0.y.z are treated as initial development (no bincompat guarantees)
ThisBuild / versionScheme :=  Some("semver-spec")
