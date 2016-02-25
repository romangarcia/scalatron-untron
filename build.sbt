organization := "com.tronador"

name := "untron"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"             % "2.2.2"    % "test"
)

val copyBot = TaskKey[Unit]("copyBot", "Copies your bot over!")

val botDirectoryTask = SettingKey[File]("bot-directory")

botDirectoryTask := file("bots")

copyBot <<= (botDirectoryTask, name, (Keys.`package` in Compile)) map { (botDirectory, name, botJar)   =>
   IO createDirectory (botDirectory / name)
   //Scalatron explicitly requires "ScalatronBot.jar"
   IO copyFile (botJar, botDirectory / name / "ScalatronBot.jar")
}

val play  = TaskKey[Unit]("play", "Runs your scalatron bot!")

play <<= (botDirectoryTask,(dependencyClasspath in Compile),(unmanagedClasspath in Compile),(Keys.     `package` in Compile) ) map { (botDirectory,dependencyClasspath,unmanagedClasspath, botJar) =>
   val cmd = "java" :: "-cp" :: Seq(Seq(botJar),dependencyClasspath.files,unmanagedClasspath.files).  flatten.absString :: "scalatron.main.Main" :: "-plugins" :: botDirectory.absolutePath :: Nil
   println(cmd)
   cmd !
}

play <<= play.dependsOn(copyBot)
