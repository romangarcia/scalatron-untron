class Logger(n: Class[_], debugMode: Boolean = false) {

  def debug(m: => String) = if (debugMode) {
    println(m)
  }

  def info(m: => String) = println(m)

}
