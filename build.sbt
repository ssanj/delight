lazy val scala212 = "2.12.14"

lazy val scala213 = "2.13.6"

lazy val supportedScalaVersions = List(scala212, scala213)

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

lazy val scalacSettings = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12))=> scalacOptionsIn212
    case Some((2, 13))=> scalacOptionsIn213
    case _ => Seq.empty[String]
  }
}

lazy val commonSettings = Seq(
  // organization := "net.ssanj",
  Compile / compile / wartremoverErrors ++= Warts.allBut(Wart.Any)
)

lazy val scalaTest = "org.scalatest"  %% "scalatest"   % "3.0.8"

lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.15.4" % Test

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
    // licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
    libraryDependencies ++= Seq(scalaTest % Compile, scalaCheck)
)
