trait BotHandler extends (String => String) {

  def welcome(welcome: Welcome): BotOutput
  def reactMaster(react: React): BotOutput
  def reactMini(react: React): MiniBotOutput
  def goodBye(goodbye: Goodbye): Idle.type

  def apply(input: String): String = {

    val in: BotInput = BotInput(input)

    in match {
      case w: Welcome => welcome(w).toCommandString
      case rm: React if rm.isMaster => reactMaster(rm).toCommandString
      case rmi: React => reactMini(rmi).toCommandString
      case g: Goodbye => goodBye(g).toCommandString
    }
  }

}

sealed trait BotInput
case class Welcome(name: String, apocalypse: Int, round: Int) extends BotInput
case class React(name: String, view: BotView, time: Int, energy: Int, master: Option[BotDirection], collision: Option[BotDirection], generation: Int) extends BotInput {
  def isMaster: Boolean = generation == 0
}
case class Goodbye(energy: Int) extends BotInput

sealed trait BotOutput {
  def toCommandString: String
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
case class Set(keyValues: (String, String)*) extends MiniBotOutput with MasterBotOutput{
  def toCommandString: String = s"Set(${keyValues.map{case (k, v) => s"$k=$v" }.mkString(",")})"
}
case class Spawn(direction: BotDirection, name: String, energy: Int) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = s"Spawn(direction=$direction,name=$name,energy=$energy)"
}
case object Idle extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = ""
}
case class CompositeOutput(say: Option[Say] = None,
                           status: Option[Status] = None,
                           spawn: Option[Spawn] = None,
                           set: Option[Set] = None,
                           explode: Option[Explode] = None,
                           move: Option[Move] = None) extends MiniBotOutput with MasterBotOutput {
  def toCommandString: String = {
    val commands: Seq[BotOutput] = Seq(say, status, spawn, set, explode, move).flatten
    commands.map(_.toCommandString).mkString("|")
  }
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

  def offsetToNearest(c: Char) = {
    val relativePositions = cells.par.view.zipWithIndex.filter(_._1 == c).map(p => relPosFromIndex(p._2))
    if(relativePositions.isEmpty) None
    else Some(relativePositions.minBy(_.length))
  }
}

object BotDirection {
  def apply(s: String): BotDirection = {
    s.split(':') match {
      case Array(x, y) => new BotDirection(x.toInt, y.toInt)
    }
  }
}
case class BotDirection(x: Int, y: Int) {
  override def toString = x + ":" + y
  def +(pos: BotDirection) = BotDirection(x+pos.x, y+pos.y)
  def -(pos: BotDirection) = BotDirection(x-pos.x, y-pos.y)
  def distance(pos: BotDirection) : Double = (this-pos).length
  def length : Double = math.sqrt(x*x + y*y)
  def signum = BotDirection(x.signum, y.signum)
}

object BotInput {

  def apply(in: String): BotInput = {
    val (opCode, p) = parseInput(in)

    opCode match {
      case "Welcome" => Welcome(p("name"), p("apocalypse").toInt, p("round").toInt)
      case "React" => React(p("name"), new BotView(p("view")),
        p("time").toInt, p("energy").toInt,
        p.get("master").map(s => BotDirection(s)),
        p.get("collision").map(s => BotDirection(s)), p("generation").toInt
      )
      case "Goodbye" => Goodbye(p("energy").toInt)
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