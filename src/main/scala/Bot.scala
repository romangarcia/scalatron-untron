import untron._

class ControlFunctionFactory {

  def create: String => String = new ControlFunction()

}

class ControlFunction extends BotHandler {
  override def welcome(welcome: Welcome): BotOutput = Say("Hello!")

  override def goodBye(goodbye: Goodbye): Idle.type = Idle

  override def reactMini(react: React): BotOutput = Idle

  override def reactMaster(react: React): BotOutput = Idle
}