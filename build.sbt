lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
  organization := "net.ssanj",
  version := "0.0.2",
  scalacOptions ++= Seq(
                      "-unchecked",
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
)

lazy val scalaTest = "org.scalatest"  %% "scalatest"   % "3.0.5"

lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

lazy val parent = (project in file("."))
  .dependsOn(sample)
  .settings(
    commonSettings,
    publish := {},
    publishLocal := {}
  )

lazy val sample = (project in file("sample"))
  .dependsOn(delight)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(scalaTest % Test),
    publish := {},
    publishLocal := {}
  )

lazy val delight = (project in file("delight"))
  .settings(
    commonSettings,
    licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
    libraryDependencies ++= Seq(scalaTest % Compile, scalaCheck)
)