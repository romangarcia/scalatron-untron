import org.scalatest.FlatSpec
import untron._

class BotInputSpec extends FlatSpec {

  "BotInput" should "parse Welcome command" in {
    val in = BotInput("Welcome(name=Pepe,apocalypse=5000,round=1)")
    assert( in === Welcome(name="Pepe",apocalypse=5000,round=1) )
  }

  it should "parse React command" in {
    val in = BotInput("React(generation=0,name=Pepe,time=100,view=___W___W___W___,energy=1000,master=0:0,collision=0:0)")
    assert( in === React(name="Pepe",time=100,view=new BotView("___W___W___W___"),energy=1000,master=BotDirection(0, 0),collision=Some(BotDirection(0,0)), generation=0) )
  }
}
