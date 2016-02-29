import tronador._
import untron.{MiniBot, MasterBot}

class ControlFunctionFactory {
  def create: String => String = new ControlFunction()
}

class ControlFunction extends BotHandler {
  override def welcome(welcome: WelcomeCommand): BotOutput = Idle

  override def goodBye(goodbye: GoodbyeCommand): Idle.type = Idle

  override def reactMini(reactCmd: ReactCommand): MiniBotOutput = MiniBot(reactCmd).react()

  override def reactMaster(reactCmd: ReactCommand): MasterBotOutput = MasterBot(reactCmd).react()

}

