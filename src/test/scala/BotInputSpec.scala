import org.scalatest.FlatSpec

class BotInputSpec extends FlatSpec {

  "BotInput" should "parse Welcome command" in {
    val in = BotInput("Welcome(name=Pepe,apocalypse=5000,round=1)")
    assert( in === Welcome(name="Pepe",apocalypse=5000,round=1) )
  }

  it should "parse React minibot command" in {
    val in = BotInput("React(generation=0,name=Pepe,time=100,view=___W___W___W___,energy=1000,master=0:0,collision=0:0)")
    assert( in === React(name="Pepe",time=100,view=new BotView("___W___W___W___"),energy=1000,master=Some(BotDirection(0, 0)),collision=Some(BotDirection(0,0)), generation=0, params = Map()) )
  }

  it should "parse React masterbot command" in {
    val in = BotInput("React(generation=0,time=0,view=________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________M________________________________________________________________________________________________________________________________________________________________________________________________________________________________________WWW____________________________?___________________________WWW________WWWW__________________?_______?W???_________________________??W??_________________________??????_________________________?????_________________________??????_____________________,energy=1000,slaves=0,name=untron)")
    assert( in === React(name="Pepe",time=100,view=new BotView("___W___W___W___"),energy=1000,master=None,collision=Some(BotDirection(0,0)), generation=0, params = Map()) )
  }
}
