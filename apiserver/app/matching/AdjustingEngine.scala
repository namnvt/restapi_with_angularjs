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

  def acceptPost(order: Posting) {

    val (book, counterBook) = getBooks(order.side)
    book.add(order)
  }
  def matchPredictAll () {
    val totalBuy = buy.postings().map ( _.quantity ).sum
    val totalSell = sell.postings().map ( _.quantity ).sum
    val percentBuy = if (totalBuy >= totalSell)  100 else totalBuy/totalSell*100
    val percentSell = if (totalSell >= totalBuy)  100 else totalSell/totalBuy*100
    println ("Total Buy%:" + percentBuy)
    println ("Total Sell%:" + percentSell)
  }
  
  def matchPredictBuy() {
    var buyPrices = buy.prices()
    var sellPrices = sell.prices()
    calcPercentMatch(buyPrices,sellPrices)
    
    def calcPercentMatch (bprices:List[(Double,Double)],sprices:List[(Double,Double)]) {
        if (bprices.isEmpty) return
        bprices.head match {
          case (price,qty) => {
            calcPercentMatch(bprices.tail, reduceMatched((price,qty), sprices))
          }
          case _ => println("1")
        }
    }
    
    def reduceMatched (b:(Double,Double), sprices:List[(Double,Double)]):List[(Double,Double)] = {
      println("Reduce:"+ b +"List:"+ sprices)
      sprices.head match {
          case (price,qty) => {
            if (b._1 >= price) {
              if (b._2 == qty) sprices.tail
              else if (b._2 > qty) reduceMatched((b._1,b._2-qty),sprices.tail) 
              else (b._1,qty-b._2) :: sprices.tail
            } else sprices
          }
          case _ => sprices
      }
    }
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
