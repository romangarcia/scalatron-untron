organization := "com.tronador"

name := "untron"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"             % "2.2.2"    % "test"
)

val copyBot = TaskKey[Unit]("copyBot", "Copies your bot over!")

val scalatronDirectoryTask = SettingKey[File]("scalatron-directory")

// val botDirectoryTask = SettingKey[File]("bot-directory")

// val webuiDirectoryTask = SettingKey[File]("webui-directory")

scalatronDirectoryTask := file("/home/rogarcia/dev/tools/Scalatron/")

// botDirectoryTask := file("/home/rogarcia/dev/tools/Scalatron/bots")

// webuiDirectoryTask := file("/home/rogarcia/dev/tools/Scalatron/webui")

copyBot <<= (scalatronDirectoryTask, name, (Keys.`package` in Compile)) map { (scalatronDirectory, name, botJar)   =>
   IO createDirectory (scalatronDirectory / "bots" / name)
   //Scalatron explicitly requires "ScalatronBot.jar"
   IO copyFile (botJar, scalatronDirectory / "bots" / name / "ScalatronBot.jar")
}

val play  = TaskKey[Unit]("play", "Runs your scalatron bot!")

play <<= (scalatronDirectoryTask, (dependencyClasspath in Compile),(unmanagedClasspath in Compile),(Keys.`package` in Compile) ) map {
  (scalatronDirectory, dependencyClasspath,unmanagedClasspath, botJar) =>
    val cp = Seq(Seq(botJar),dependencyClasspath.files,unmanagedClasspath.files).flatten.absString
    // val cmd = "java" :: "-cp" :: cp :: "scalatron.main.Main" :: "-plugins" :: botDirectory.absolutePath :: "-webui" :: webuiDirectory.absolutePath :: Nil
    val cmd = (scalatronDirectory / "bin" / "startServer.sh").absolutePath
    println(cmd)
    cmd !
}

play <<= play.dependsOn(copyBot)
