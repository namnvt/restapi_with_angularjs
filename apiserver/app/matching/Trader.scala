package matching

/**
 * @author nam.nvt
 */
trait Trader {
   val name: String
   val rank: Int
}

case class Buyer(name:String, rank: Int) extends Trader 
case class Seller(name:String, rank: Int) extends Trader 