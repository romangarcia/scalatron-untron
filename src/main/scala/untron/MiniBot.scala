package untron

import tronador._

case class MiniBot(reactCmd: ReactCommand) {

  private val log = Logger(getClass)

  def react(): MiniBotOutput = {
    import reactCmd._
    import Entities._

    val out = CompositeBotOutput()

    def targetTo(c: Char): Option[CompositeBotOutput] = {
      view.offsetToNearest(c).map { bd =>
        log.infoLn(s"WILL ATTEMPT TARGET TO $c: $bd")
        out.move(bd.signum).set("target" -> bd)
      }
    }

    reactCmd.params.get("target").map(BotDirection(_)).map { bd =>
      // keep moving to existing target
      log.infoLn(s"WILL CONTINUE PURSUE TO $bd")
      out.move(bd.signum)
    }.orElse {
      // search slave as new target
      targetTo(ENEMY_SLAVE)
    }.orElse {
      // search slave as new target
      targetTo(ENEMY_MASTER)
    }.orElse {
      // look out for papa
      master.map { bd =>
        log.infoLn(s"WILL RETURN TO MASTER: $bd")
        out.move(bd).set("target" -> bd).set("last_direction" -> bd)
      }
    }.getOrElse {
      out.say("What should I do?")
    }
  }

}
