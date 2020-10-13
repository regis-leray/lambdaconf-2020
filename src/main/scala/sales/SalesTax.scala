package sales

/**
 *  +++ Function composition +++
 *
 *  ### Goal ###
 *  - Interview exercise for a java position, wrote 14 files / class :8
 *  - No design pattern such as strategy / visitor
 *  - Program with only functions / remove objects
 *
 *  ### Sales Tax ###
 *  - Write an application that prints out the receipt details for these shopping baskets.
 *  - Tax rate of 10% on all goods, except books, food, and medical products that are exempt.
 *  - Import duty is an additional sales tax applicable on all imported goods at a rate of 5%, with no exemptions.
 *
 ** =================
 * INPUT:
 *
 * Input 1:
 * 1 book at 12.49
 * 1 music CD at 14.99
 * 1 chocolate bar at 0.85
 * 1 bottle of perfume at 18.99
 *
 * Input 2:
 * 1 imported box of chocolates at 10.00
 * 1 imported bottle of perfume at 47.50
 * 1 packet of headache pills at 9.75
 *
 * OUTPUT:
 *
 * Output 1:
 * 1 book : 12.49
 * 1 music CD: 16.49
 * 1 chocolate bar: 0.85
 * 1 bottle of perfume : 20.89
 * Sales Taxes: 3.40
 * Total: 50.72
 *
 * Output 2:
 * 1 imported box of chocolates: 10.50
 * 1 imported bottle of perfume: 54.65
 * 1 packet of headache pills : 9.75
 * Sales Taxes: 7.63
 * Total: 74.88
 *
 */

object SalesTax {

  sealed abstract class ItemCategory(val name: String)

  object ItemCategory{
    //GoodTax 10%
    case object Cd extends ItemCategory("Music CD")
    case object BottleParfum extends ItemCategory("bottle of perfume")

    //ImportTax 5%
    case object ImportedChocolateBox extends ItemCategory("box of imported chocolates")

    //ImportTax + GoodTax
    case object ImportedParfum extends ItemCategory("imported bottle of perfume")

    //FreeTax 0%
    case object Book extends ItemCategory("book")
    case object ChocolateBar extends ItemCategory ("chocolate bar")
    case object HeadachePills extends ItemCategory("packet of headache pills")
  }

  case class Item(q: Int, price: Double, cat: ItemCategory)

  type ShoppingCart = List[List[Item]]

  type TaxCalculator = Item => Double
  val taxCalculator: TaxCalculator = ???

  type InputPrinter = List[Item] => String
  val inputPrinter: InputPrinter = {
    val _ = "1 bottle of perfume at 18.99"
    ???
  }

  type OutputPrinter = List[Item] => String
  val outputPrinter: OutputPrinter = {

    """1 bottle of perfume : 20.89
      |Sales Taxes: 3.40
      |Total: 50.72
      |""".stripMargin
    ???
  }

  def print(inputPrinter: InputPrinter, outputPrinter: OutputPrinter): ShoppingCart => String = {
    """
      |INPUT:
      |
      |Input 1:
      |
      |OUTPUT:
      |
      |Output 1:""".stripMargin

    ???
  }


  def main(args: Array[String]): Unit = {
    import ItemCategory._
    val printReceipt: ShoppingCart => String = ???

    val shoppingCart: ShoppingCart = List(
      List(
        Item(1, 12.49, Book),
        Item(1, 14.99, Cd),
        Item(1, 0.85, ChocolateBar),
        Item(1, 18.99, BottleParfum)
      ),
      List(
        Item(1, 10.00, ImportedChocolateBox),
        Item(1, 47.50, ImportedParfum),
        Item(1, 9.75, HeadachePills)
      )
    )

    println(
      printReceipt(shoppingCart)
    )
  }
}

