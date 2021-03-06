package matching

/**
 * User: namnvt
 * Date: 08/24/2015
 * Time: 05:00 PM
 */
 
trait PostingHelper {
  def bookDisplay: String


  def crossesAt(matchedprice: Double): Boolean

  def decreasedBy(qty: Double): Posting
}

object PostingHelper {

  def all(): PartialFunction[Posting, PostingHelper] = {

    case self@Posting(_, side, _, price,_,_) => new PostingHelper {
      def bookDisplay: String = price.toString


      def crossesAt(matchedprice: Double): Boolean = side match {
        case Buy => matchedprice <= price
        case Sell => matchedprice >= price
      }

      def decreasedBy(qty: Double): Posting =
        self.copy(quantity = self.quantity - qty)
    }
  }
}
