package tronador

object Logger {
  private[Logger] val debugMode = false

  def apply(n: Class[_]): Logger = new Logger(n, debugMode)
}

class Logger(n: Class[_], debugMode: Boolean) {

  def debugLn(m: => String) = if (debugMode) {
    println(m)
  }
  def debug(m: => String) = if (debugMode) {
    print(m)
  }

  def infoLn(m: => String) = println(m)
  def info(m: => String) = print(m)
}
