import org.scalatest.FlatSpec

class BotOutputSpec extends FlatSpec {

  "BotOutput" should "render Move command" in {
    val out = Move(BotDirection(5, -3)).toCommandString
    assert( out === "Move(direction=5:-3)" )
  }

  it should "render Spawn command" in {
    val out = Spawn(BotDirection(5, -3), 1000, Option("Pepe")).toCommandString
    assert( out === "Spawn(direction=5:-3,name=Pepe,energy=1000)" )
  }

  it should "render Say command" in {
    val out = Say("Hola").toCommandString
    assert( out === "Say(text=Hola)" )
  }

  it should "render Set command" in {
    val out = Set("foo" -> "bar", "fez" -> "pez").toCommandString
    assert( out === "Set(foo=bar,fez=pez)" )
  }

  it should "render Explode command" in {
    val out = Explode(5000).toCommandString
    assert( out === "Explode(size=5000)" )
  }

  it should "render Composite command" in {
    val out = CompositeBotOutput(explode = Some(Explode(3000)), say = Some(Say("Hola"))).toCommandString
    assert( out === "Say(text=Hola)|Explode(size=3000)" )
  }

}
