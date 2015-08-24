package matching

import collection.mutable

/**
 * User: namnvt
 * Date: 08/24/2015
 * Time: 05:00 PM
 */

class AdjustingEngine(buy: PostingBook, sell: PostingBook, postingHelpers: (Posting => PostingHelper))
  extends mutable.Publisher[AdjustEvent] {

  def acceptOrder(order: Posting) {

    val (book, counterBook) = getBooks(order.side)
    val unfilledOrder = tryMatch(order, counterBook)
    unfilledOrder.foreach(book.add(_))
  }


  private def getBooks(side: Side): (PostingBook, PostingBook) = side match {
    case Buy => (buy, sell)
    case Sell => (sell, buy)
  }

  private def tryMatch(posting: Posting, counterBook: PostingBook): Option[Posting] = {

    if (posting.quantity == 0) None
    else counterBook.top match {
      case None => Some(posting)
      case Some(top) => tryMatchWithTop(posting, top) match {
        case None => Some(posting)
        case Some(trade) => {
          counterBook.decreaseTopBy(trade.qty)
          publish(trade)
          val unfilledOrder = postingHelpers(posting).decreasedBy(trade.qty)
          tryMatch(unfilledOrder, counterBook)
        }
      }
    }
  }

  private def tryMatchWithTop(posting: Posting, top: Posting): Option[Match] = top match {

    case topLimit: Posting => {
      if (postingHelpers(posting).crossesAt(topLimit.price)) {
        val (buy, sell) = if (posting.side == Buy) (posting, topLimit) else (topLimit, posting)
        Some(Match(buy.trader.name, sell.trader.name, topLimit.price, math.min(buy.quantity, sell.quantity)))
      }
      else None
    }
  }
}
