import org.scalatest.FlatSpec
import tronador.{BotMap, BotView}

class BotMapSpec extends FlatSpec with TestData {

  "BotMap" should "print debug map" in {

    val view = new BotView(viewCells)
    val map = BotMap(view)

  }
}
