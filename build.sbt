enablePlugins(ScalaJSPlugin)

name := "lavamaze"
organization := "com.wbillingsley"
scalaVersion := "3.3.4"

// Don't automatically call main
scalaJSUseMainModuleInitializer := false

updateOptions := updateOptions.value.withLatestSnapshots(false)

val veautifulVersion = "0.3-M6"

// For Circuits Up!
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "com.wbillingsley" %%% "doctacular" % veautifulVersion,
	"com.wbillingsley" %%% "scatter" % veautifulVersion,
  "com.github.theintelligentbook" % "circuitsup" % "master-SNAPSHOT"
)

val deployScript = taskKey[Unit]("Copies the fullOptJS script to deployscripts/")
val fastDeploy = taskKey[Unit]("Copies the fastOptJS script to deployscripts/")

// Used by Travis-CI to get the script out from the .gitignored target directory
// Don't run it locally, or you'll find the script gets loaded twice in index.html!
deployScript := {
  val opt = (Compile / fullOptJS).value
  IO.copyFile(opt.data, new java.io.File("deployscripts/compiled.js"))
}

fastDeploy := {
  val opt = (Compile / fastOptJS).value
  IO.copyFile(opt.data, new java.io.File("deployscripts/compiled.js"))
}