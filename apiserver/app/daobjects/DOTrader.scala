package daobjects

/**
 * @author nam.nvt
 */
abstract class DOTrader {
   var name: String
   var rank: Int

}

case class DOBuyer(name:String, rank: Int) extends DOTrader {
  def name_=(x$1: String): Unit = ???
  def rank_=(x$1: Int): Unit = ???

}
case class DOSeller(name:String, rank: Int) extends DOTrader {
  def name_=(x$1: String): Unit = ???
  def rank_=(x$1: Int): Unit = ???

}