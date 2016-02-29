import scala.util.Random

class ControlFunctionFactory {
  def create: String => String = new ControlFunction()
}

class ControlFunction extends BotHandler {
  override def welcome(welcome: Welcome): BotOutput = {
    Status("...")
  }

  override def goodBye(goodbye: Goodbye): Idle.type = Idle

  /*
  MOOD: manejar un mood actual Set(mood=Lurking / Aggresive / Defensive)?
    - aggresive: si no tenemos un target, buscar uno (el mas cercano?) y marcar --> Set(target=x:y) -- MISIL
    - defensive: defender el master, bloqueando el paso de otros slaves?
    - lurking: buscar comida
    - block: proteger el master (explotar ante beasts)
    - group-attack:
   */
  override def reactMini(react: React): MiniBotOutput = {
    import BotMap._
    import react._

    val out = CompositeBotOutput()

    def targetTo(c: Char): Option[CompositeBotOutput] = {
      view.offsetToNearest(c).map { bd =>
        println(s"WILL ATTEMPT TARGET TO $c: $bd")
        out.move(bd.signum).set("target" -> bd)
      }
    }

    react.params.get("target").map(BotDirection(_)).map { bd =>
      // keep moving to existing target
      println(s"WILL CONTINUE PURSUE TO $bd")
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
        println(s"WILL RETURN TO MASTER: $bd")
        out.move(bd).set("target" -> bd).set("last_direction" -> bd)
      }
    }.getOrElse {
      out.say("What should I do?")
    }

  }

  val rnd = new Random()

  override def reactMaster(react: React): MasterBotOutput = {
    import BotMap._

    val map = {
      val m = BotMap(react.view)
      react.params.get("last_direction").map { bds =>
        m.withLastDirectionWeight(BotDirection(bds))
      }.getOrElse(m)
    }

    val bestDirection = map.bestDirection

    val out = CompositeBotOutput()

    val outWithSpawn = if (react.energy > 100) {
      react.view.offsetToNearest(c => c.isUpper && c != WALL).map { bd =>
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

object BotMap {

  val ENEMY_MASTER = 'm'
  val ENEMY_SLAVE = 's'
  val ENEMY_BEAST = 'b'
  val ENEMY_PLANT = 'p'

  val FRIEND_MASTER = 'M'
  val FRIEND_SLAVE = 'S'
  val FRIEND_PLANT = 'P'
  val FRIEND_BEAST = 'B'

  val WALL = 'W'

  val Up = BotDirection(0, -1)
  val RightUp = BotDirection(1, -1)
  val Right = BotDirection(1, 0)
  val RightDown = BotDirection(1, 1)
  val Down = BotDirection(0, 1)
  val LeftDown = BotDirection(-1, 1)
  val Left = BotDirection(-1, 0)
  val LeftUp = BotDirection(-1, -1)

  def apply(view: BotView): BotMap = {

    var nearestEnemyMaster: Option[BotDirection] = None
    var nearestEnemySlave: Option[BotDirection] = None

    val cells = view.cells
    val cellCount = cells.length
    val map = (0 until cellCount) flatMap { i =>
      val cellRelPos = view.relPosFromIndex(i)
      if(cellRelPos.isNonZero) {
        val stepDistance = cellRelPos.stepCount
        val value: Double = cells(i) match {
          case ENEMY_MASTER => // another master: not dangerous, but an obstacle
            nearestEnemyMaster = Some(cellRelPos)
            if(stepDistance < 4) -1000 else 0

          case ENEMY_SLAVE => // another slave: potentially dangerous?
            nearestEnemySlave = Some(cellRelPos)
            -100 / stepDistance

          case FRIEND_SLAVE => // our own slave
            0.0

          case FRIEND_BEAST => // good beast: valuable, but runs away
            if(stepDistance == 1) 600
            else if(stepDistance == 2) 300
            else (150 - stepDistance * 15).max(10)

          case FRIEND_PLANT => // good plant: less valuable, but does not run
            if(stepDistance == 1) 500
            else if(stepDistance == 2) 300
            else (150 - stepDistance * 10).max(10)

          case ENEMY_BEAST => // bad beast: dangerous, but only if very close
            if(stepDistance < 4) -400 / stepDistance else -50 / stepDistance

          case ENEMY_PLANT => // bad plant: bad, but only if I step on it
            if(stepDistance < 2) -1000 else 0

          case WALL => // wall: harmless, just don't walk into it
            if(stepDistance < 2) -1000 else 0

          case _ => 0.0
        }

        Some(cellRelPos -> value)
      } else None
    }

    val completeMap = map.toMap

    debugBotMap(completeMap, view)

    new BotMap(completeMap, nearestEnemyMaster, nearestEnemySlave)
  }

  def debugBotMap(m: Map[BotDirection, Double], view: BotView): Unit = {

    val newMap = m.map { case (bd, w) =>
      view.absPosFromRelPos(bd) -> (w -> view.cellAtRelPos(bd))
    }

    for (y <- 0 to 30) {
      for (x <- 0 to 30) {
        val repr = newMap.get(BotDirection(x, y)).map { case (w, c) =>
          //s"$c[${w.toInt}]"
          s"$c"
        }.getOrElse("M")
        print(repr)
      }
      println
    }

  }


}

case class BotMap(directionWeights: Map[BotDirection, Double],
                  nearEnemyMaster: Option[BotDirection],
                  nearEnemySlave: Option[BotDirection]) {
  lazy val bestDirection: BotDirection = directionWeights.maxBy(_._2)._1

  def withLastDirectionWeight(bd: BotDirection) = {
    copy(directionWeights = directionWeights.updated(bd, directionWeights(bd) + 50))
  }
}