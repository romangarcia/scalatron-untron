package tronador

import tronador.Entities._

object BotMap {

  private val log = Logger(getClass)

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
        val entity = Entities.valueOf(cells(i))
        val value: Double = entity match {
          case EnemyMaster => // another master: not dangerous, but an obstacle
            nearestEnemyMaster = Some(cellRelPos)
            if(stepDistance < 4) -1000 else 0

          case EnemySlave => // another slave: potentially dangerous?
            nearestEnemySlave = Some(cellRelPos)
            -100 / stepDistance

          case FriendSlave => // our own slave
            0.0

          case FriendBeast => // good beast: valuable, but runs away
            if(stepDistance == 1) 600
            else if(stepDistance == 2) 300
            else (150 - stepDistance * 15).max(10)

          case FriendPlant => // good plant: less valuable, but does not run
            if(stepDistance == 1) 500
            else if(stepDistance == 2) 300
            else (150 - stepDistance * 10).max(10)

          case EnemyBeast => // bad beast: dangerous, but only if very close
            if(stepDistance < 4) -400 / stepDistance else -50 / stepDistance

          case EnemyPlant => // bad plant: bad, but only if I step on it
            if(stepDistance < 2) -1000 else 0

          case Wall => // wall: harmless, just don't walk into it
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
          s"$c"
        }.getOrElse("M")
        log.debug(repr)
      }
      log.debugLn("")
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