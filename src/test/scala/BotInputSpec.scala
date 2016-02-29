import org.scalatest.FlatSpec
import tronador._

class BotInputSpec extends FlatSpec with TestData {

  "BotInput" should "parse Welcome command" in {
    val in = BotInput("Welcome(name=Pepe,apocalypse=5000,round=1)")
    assert( in === WelcomeCommand(name="Pepe",apocalypse=5000,round=1) )
  }

  it should "parse React minibot command" in {
    val in = BotInput("React(generation=0,name=Pepe,time=100,view=___W___W___W___,energy=1000,master=0:0,collision=0:0)")
    assert( in === ReactCommand(name="Pepe",time=100,view=new BotView("___W___W___W___"),
      energy=1000,master=Some(BotDirection(0, 0)),collision=Some(BotDirection(0,0)), generation=0,
      params = Map("name" -> "Pepe", "master" -> "0:0", "collision" -> "0:0", "generation" -> "0", "energy" -> "1000", "time" -> "100", "view" -> "___W___W___W___")
    ) )
  }

  it should "parse React masterbot command" in {
    val in = BotInput(s"React(generation=0,time=100,view=$viewCells,energy=1000,slaves=0,name=untron)")

    assert( in === ReactCommand(name="untron",time=100,view=new BotView(viewCells),
      energy=1000,master=None,collision=None, generation=0,
      params = Map("name" -> "untron", "generation" -> "0", "energy" -> "1000", "time" -> "100", "slaves" -> "0", "view" -> viewCells)
    ))
  }
}
