enablePlugins(ScalaJSPlugin)

name := "lavamaze"
organization := "com.wbillingsley"
scalaVersion := "3.1.0"

// Don't automatically call main
scalaJSUseMainModuleInitializer := false

resolvers += "jitpack" at "https://jitpack.io"

updateOptions := updateOptions.value.withLatestSnapshots(false)

libraryDependencies ++= Seq(
//  "org.scala-js" %%% "scalajs-dom" % "1.1.0",
  "com.github.wbillingsley.veautiful" %%% "veautiful" % "v0.2-SNAPSHOT",
  "com.github.wbillingsley.veautiful" %%% "veautiful-templates" % "v0.2-SNAPSHOT",
	"com.github.wbillingsley.veautiful" %%% "scatter" % "v0.2-SNAPSHOT"
)

val deployScript = taskKey[Unit]("Copies the fullOptJS script to deployscripts/")

// Used by Travis-CI to get the script out from the .gitignored target directory
// Don't run it locally, or you'll find the script gets loaded twice in index.html!
deployScript := {
  val opt = (Compile / fullOptJS).value
  IO.copyFile(opt.data, new java.io.File("deployscripts/compiled.js"))
}