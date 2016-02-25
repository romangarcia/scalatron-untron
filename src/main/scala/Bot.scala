class ControlFunctionFactory {
  def create: String => String = new ControlFunction()
}

class ControlFunction extends BotHandler {
  override def welcome(welcome: Welcome): BotOutput = {
    Status("...")
  }

  override def goodBye(goodbye: Goodbye): Idle.type = Idle

  override def reactMini(react: React): MiniBotOutput = Idle

  override def reactMaster(react: React): MasterBotOutput =
    CompositeOutput(status = Some(Status("FFFFUUUUUUuuuu")), move = Some(Move(BotDirection(1,1))))

}