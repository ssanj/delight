lazy val scala212 = "2.12.8"

lazy val scala213 = "2.13.0"

lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion := scala212

lazy val commonSettings = Seq(
  organization := "net.ssanj",
  version := "0.0.3-SNAPSHOT",
  scalacOptions ++= Seq(
                      "-unchecked",
                      "-deprecation",
                      "-feature",
                      "-Xfatal-warnings",
                      "-Xlint:_",
                      // "-Ywarn-dead-code",
                      // "-Wwarn-inaccessible",
                      // "-Ywarn-unused-import",
                      // "-Ywarn-infer-any",
                      // "-Ywarn-nullary-override",
                      // "-Ywarn-nullary-unit"
                     ),
  wartremoverErrors in (Compile, compile) ++= Warts.allBut(Wart.Any)
)

lazy val scalaTest = "org.scalatest"  %% "scalatest"   % "3.0.8"

lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

lazy val parent = (project in file("."))
  .aggregate(delight, sample)
  .settings(
    commonSettings,
    publish := {},
    publishLocal := {}
  )

lazy val sample = (project in file("sample"))
  .dependsOn(delight)
  .settings(
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(scalaTest % Test),
    publish := {},
    publishLocal := {}
  )

lazy val delight = (project in file("delight"))
  .settings(
    commonSettings,
    crossScalaVersions := supportedScalaVersions,
    licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
    libraryDependencies ++= Seq(scalaTest % Compile, scalaCheck)
)