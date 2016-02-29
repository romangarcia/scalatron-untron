package tronador

import scala.util.Random

trait BotHandler extends (String => String) {

  def welcome(welcome: WelcomeCommand): BotOutput
  def reactMaster(react: ReactCommand): BotOutput
  def reactMini(react: ReactCommand): MiniBotOutput
  def goodBye(goodbye: GoodbyeCommand): Idle.type

  def apply(input: String): String = {

    val in: BotInput = BotInput(input)

    (in match {
      case w: WelcomeCommand => welcome(w)
      case rm: ReactCommand if rm.isMaster => reactMaster(rm)
      case rmi: ReactCommand => reactMini(rmi)
      case g: GoodbyeCommand => goodBye(g)
    }).toCommandString
  }

}

sealed trait BotInput
case class WelcomeCommand(name: String, apocalypse: Int, round: Int) extends BotInput
case class ReactCommand(name: String,
                        view: BotView,
                        time: Int,
                        energy: Int,
                        master: Option[BotDirection],
                        collision: Option[BotDirection],
                        generation: Int,
                        params: Map[String, String]
                ) extends BotInput {
  def isMaster: Boolean = generation == 0
}
case class GoodbyeCommand(energy: Int) extends BotInput

sealed trait BotOutput {
  def toCommandString: String

  def paramsToCommandString(m: Map[String, String]) = {
    if (m.isEmpty) ""
    else {
      m.map{case (k, v) => s"$k=$v" }.mkString(",")
    }
  }
}

sealed trait MiniBotOutput extends BotOutput
sealed trait MasterBotOutput extends BotOutput

case class Move(direction: BotDirection) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Move(direction=${direction.x}:${direction.y})"
}
case class Explode(size: Int) extends MiniBotOutput {
  def toCommandString: String = s"Explode(size=$size)"
}
case class Say(text: String) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Say(text=$text)"
}
case class Status(text: String) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Status(text=$text)"
}
case class Set(keyValues: (String, String)*) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Set(${paramsToCommandString(keyValues.toMap)})"
}
case class Spawn(direction: BotDirection, energy: Int, name: Option[String], params: Map[String, String] = Map()) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Spawn(direction=$direction${name.map(n => s",name=$n").getOrElse("")},energy=$energy${paramsToCommandString(params)})"
}
case object Idle extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = ""
}
case class CompositeBotOutput(say: Option[Say] = None,
                              status: Option[Status] = None,
                              spawn: Option[Spawn] = None,
                              set: Option[Set] = None,
                              explode: Option[Explode] = None,
                              move: Option[Move] = None) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = {
    val commands: Seq[BotOutput] = Seq(say, status, spawn, set, explode, move).flatten
    commands.map(_.toCommandString).mkString("|")
  }

  def say(text: String): CompositeBotOutput = copy(say = Some(Say(text)))
  def status(text: String): CompositeBotOutput = copy(status = Some(Status(text)))
  def move(direction: BotDirection): CompositeBotOutput = copy(move = Some(Move(direction)))
  def set(keyValue: (String, Any)): CompositeBotOutput = copy(set = set.map { s =>
    Set(s.keyValues :+ (keyValue._1 -> keyValue._2.toString): _*)
  }.orElse(Some(Set(keyValue._1 -> keyValue._2.toString))))
  def set(keyValues: Seq[(String, String)]): CompositeBotOutput = copy(set = Some(Set(keyValues:_*)))
  def spawn(direction: BotDirection, energy: Int,
            name: Option[String] = None, params: Seq[(String, String)] = Seq()) = copy(spawn = Some(Spawn(direction, energy, name, params.toMap)))
  def spawn(direction: BotDirection, energy: Int,
            params: (String, String)*) = copy(spawn = Some(Spawn(direction, energy, None, params.toMap)))
  def explode(size: Int): CompositeBotOutput = copy(explode = Some(Explode(size)))

}

case class BotView(cells: String) {
  val size = math.sqrt(cells.length).toInt
  val center = BotDirection(size/2, size/2)

  def indexFromAbsPos(absPos: BotDirection) = absPos.x + absPos.y * size
  def absPosFromIndex(index: Int) = BotDirection(index % size, index / size)
  def absPosFromRelPos(relPos: BotDirection) = relPos + center
  def cellAtAbsPos(absPos: BotDirection) = cells.charAt(indexFromAbsPos(absPos))

  def indexFromRelPos(relPos: BotDirection) = indexFromAbsPos(absPosFromRelPos(relPos))
  def relPosFromAbsPos(absPos: BotDirection) = absPos - center
  def relPosFromIndex(index: Int) = relPosFromAbsPos(absPosFromIndex(index))
  def cellAtRelPos(relPos: BotDirection) = cells.charAt(indexFromRelPos(relPos))

  def offsetToNearest(c: Char): Option[BotDirection] = offsetToNearest(_ == c)

  def offsetToNearest(f: Char => Boolean): Option[BotDirection] = {
    val matchingXY = cells.view.zipWithIndex.filter(t => f(t._1))
    if( matchingXY.isEmpty )
      None
    else {
      val nearest = matchingXY.map(p => relPosFromIndex(p._2)).minBy(_.length)
      Some(nearest)
    }
  }
}

object BotDirection {
  def apply(s: String): BotDirection = {
    s.split(':') match {
      case Array(x, y) => new BotDirection(x.toInt, y.toInt)
    }
  }

  def random(rnd: Random): BotDirection = BotDirection(rnd.nextInt(3) - 1, rnd.nextInt(3) - 1)
}
case class BotDirection(x: Int, y: Int) {
  override def toString = x + ":" + y
  def +(pos: BotDirection) = BotDirection(x+pos.x, y+pos.y)
  def -(pos: BotDirection) = BotDirection(x-pos.x, y-pos.y)
  def distance(pos: BotDirection) : Double = (this-pos).length
  def length : Double = math.sqrt(x*x + y*y)
  def signum = BotDirection(x.signum, y.signum)

  def isNonZero = x != 0 || y != 0
  def isZero = x == 0 && y == 0
  def isNonNegative = x >= 0 && y >= 0

  def stepsTo(pos: BotDirection): Int = (this - pos).stepCount // steps to reach pos: max delta X or Y
  def stepCount: Int = x.abs.max(y.abs) // steps from (0,0) to get here: max X or Y

}

object BotInput {

  def apply(in: String): BotInput = {
    val (opCode, p) = parseInput(in)

    opCode match {
      case "Welcome" => WelcomeCommand(p("name"), p("apocalypse").toInt, p("round").toInt)
      case "React" => ReactCommand(p("name"), new BotView(p("view")),
        p("time").toInt, p("energy").toInt,
        p.get("master").map(s => BotDirection(s)),
        p.get("collision").map(s => BotDirection(s)),
        p("generation").toInt,
        p
      )
      case "Goodbye" => GoodbyeCommand(p("energy").toInt)
    }

  }

  private def parseInput(in: String): (String, Map[String, String]) = {
    def splitParam(param: String) = {
      val segments = param.split('=')
      if (segments.length != 2)
        throw new IllegalStateException("invalid key/value pair: " + param)
      (segments(0), segments(1))
    }

    val segments = in.split('(')
    if (segments.length != 2)
      throw new IllegalStateException("invalid command: " + in)

    val rawParams = segments(1).dropRight(1).split(',')
    val params = rawParams.map(splitParam).toMap
    val opCode = segments.head
    opCode -> params
  }
}