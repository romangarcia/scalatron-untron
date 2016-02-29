package untron

import tronador._


case class MasterBot(reactCmd: ReactCommand) {

  private val map = {
    val m = BotMap(reactCmd.view)
    reactCmd.params.get("last_direction").map { bds =>
      m.withLastDirectionWeight(BotDirection(bds))
    }.getOrElse(m)
  }

  def react(): MasterBotOutput = {
    import Entities._

    val bestDirection = map.bestDirection

    val out = CompositeBotOutput()

    val outWithSpawn = if (reactCmd.energy > 100) {
      reactCmd.view.offsetToNearest(c => c.isUpper && c != WALL).map { bd =>
        println("There's an enemy near...ATTACK!")
        out.spawn(bd.signum, 100, "target" -> bd.toString, "mode" -> "ATTACK")
      } getOrElse {
        println("No enemies near...send a SCOUT...")
        out.spawn(BotDirection(1,1), 100, "mode" -> "SCOUT")
      }
    } else {
      // run out of here (which way to go?)
      out
    }

    // move
    outWithSpawn.move(bestDirection).set("last_direction" -> bestDirection)


  }

}
