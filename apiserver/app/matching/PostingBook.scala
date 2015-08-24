package matching


/**
 * User: namnvt
 * Date: 08/24/2015
 * Time: 05:00 PM
 */

class PostingBook(side: Side, postingHelpers: (Posting => PostingHelper)) {

  private var postingBook: List[(Double, List[Posting])] = Nil
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse

  def add(posting: Posting) {
    addPost(posting.price, posting)
  }

  def top: Option[Posting] = postingBook.headOption.map({
    case (_, orders) => orders.head
  })

  def decreaseTopBy(qty: Double) {

    postingBook match {
      case ((level, orders) :: tail) => {
        val (top :: rest) = orders
        postingBook = (qty == top.quantity, rest.isEmpty) match {
          case (true, true) => tail
          case (true, false) => (level, rest) :: tail
          case _ => (level, postingHelpers(top).decreasedBy(qty) :: rest) :: tail
        }
      }
      case Nil => throw new IllegalStateException("No top order in the empty book")
    }
  }

  def postings(): List[Posting] = postingBook.flatMap({
    case (_, postings) => postings
  })

  private def addPost(price: Double, posting: Posting) {
    def insert(list: List[(Double, List[Posting])]): List[(Double, List[Posting])] = list match {
      case Nil => List((price, List(posting)))
      case (head@(bookLevel, postings)) :: tail => priceOrdering.compare(price, bookLevel) match {
        case 0 => (bookLevel, postings :+ posting) :: tail
        case n if n < 0 => (price, List(posting)) :: list
        case _ => head :: insert(tail)
      }
    }

    postingBook = insert(postingBook)
  }
}
