package sales.solution

sealed abstract class ItemCategory(val name: String)

//10%
sealed trait GoodsTax
//5%
sealed trait ImportTax
//0%
sealed trait TaxFree

object ItemCategory{
  def unapply(arg: ItemCategory): Option[String] = Some(arg.name)

  //GoodTax 10%
  case object Cd extends ItemCategory("Music CD") with GoodsTax
  case object BottleParfum extends ItemCategory("bottle of perfume") with GoodsTax

  //ImportTax 5%
  case object ImportedChocolateBox extends ItemCategory("box of imported chocolates") with ImportTax

  //ImportTax + GoodTax
  case object ImportedParfum extends ItemCategory("imported bottle of perfume") with GoodsTax with ImportTax

  //FreeTax 0%
  case object Book extends ItemCategory("book") with TaxFree
  case object ChocolateBar extends ItemCategory ("chocolate bar") with TaxFree
  case object HeadachePills extends ItemCategory("packet of headache pills") with TaxFree
}

case class Item(q: Int, price: Double, category: ItemCategory){
  val total: Double = q * price
}

object SaleTax {
  type TaxCalculator =  Item => Double
  val calculator: TaxCalculator = {
    case Item(_, _, _: TaxFree) => 0.00
    case i@Item(_, _, _: ImportTax with GoodsTax) => i.total * 0.15
    case i@Item(_, _, _: ImportTax) => i.total * 0.05
    case i@Item(_, _,  _: GoodsTax) => i.total * 0.10
  }

  type InputPrinter = List[Item] => String
  val inputPrinter: InputPrinter = _.map{
      case Item(q, price, ItemCategory(name)) => f"$q $name at $price%1.2f"
    }.mkString("\n", "\n", "")

  type OutputPrinter = List[Item] => String
  type OutputTaxCalculatorPrinter = TaxCalculator => OutputPrinter
  val ouputPrinter: OutputTaxCalculatorPrinter = tax => items => {
    //output
    val output = items.map{
      case i@Item(q, _, ItemCategory(name)) => f"$q $name : ${ i.total + tax(i)}%1.2f"
    }.mkString("\n")

    // Total taxes
    val totalTaxes = items.map(tax).sum
    // Total
    val total = items.map(i => i.total + tax(i)).sum

    f"""
       |$output
       |Sales Taxes: $totalTaxes%1.2f
       |Total: $total%1.2f
       |""".stripMargin
  }

  type ShoppingCart = List[List[Item]]

  def print(inputPrinter: InputPrinter, outputPrinter: OutputPrinter): ShoppingCart => String = { cart =>
    val input = cart.map(inputPrinter)
      .zipWithIndex
      .map{ case (s, i) => s"\nInput ${i+1}: $s"  }
      .mkString("\n")

    val output = cart.map(outputPrinter)
      .zipWithIndex
      .map{ case (s, i) => s"\nOutput ${i+1}: $s"}
      .mkString("\n")

    s"""
      |INPUT:
      |$input
      |
      |OUTPUT:
      |$output
      |""".stripMargin
  }


  def main(args: Array[String]): Unit = {
    import ItemCategory._

    val printReceipt = print(inputPrinter, ouputPrinter(calculator))

    val shoppingCard = List(
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
      printReceipt(shoppingCard)
    )
  }
}
