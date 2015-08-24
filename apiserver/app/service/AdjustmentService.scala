package service

import matching._
import org.joda.time.DateTime


/**
 * @author namnvt
 */
object AdjustmentService extends App {
  val postingHelpers = PostingHelper.all()
  val buyBook: PostingBook = new PostingBook(Buy, postingHelpers)
  val sellBook: PostingBook = new PostingBook(Sell, postingHelpers)
  val matchingEngine = new AdjustingEngine(buyBook, sellBook, postingHelpers)

  var actualTrades = List[Match]()

  events {
    case trade: Match => actualTrades = trade :: actualTrades
  }
  private def events(handler: PartialFunction[AdjustEvent, Unit]) {
    matchingEngine.subscribe(new matchingEngine.Sub {
      def notify(pub: matchingEngine.Pub, event: AdjustEvent) {
        handler(event)
      }
    })
  }
  println("Start")
  println("Adding posts")
  var buyer1: Buyer = new Buyer("b1",1)
  var buyer2: Buyer = new Buyer("b2",2)
  var buyer3: Buyer = new Buyer("b3",3)
  
  var seller1: Seller = new Seller("s1",1)
  var seller2: Seller = new Seller("s2",2)
  var seller3: Seller = new Seller("s3",3)
  var buy1: Posting = new Posting(buyer1,Buy,100, 10.5,DateTime.now, DateTime.now)
  var buy2: Posting = new Posting(buyer2,Buy,80, 11,DateTime.now, DateTime.now)
  var buy3: Posting = new Posting(buyer3,Buy,70, 10,DateTime.now, DateTime.now)
  
  var sell1: Posting = new Posting(seller1,Sell,110, 11,DateTime.now, DateTime.now)
  var sell2: Posting = new Posting(seller2,Sell,90, 10,DateTime.now, DateTime.now)
  var sell3: Posting = new Posting(seller3,Sell,40, 9.5,DateTime.now, DateTime.now)
  
  matchingEngine.acceptOrder(buy1)
  matchingEngine.acceptOrder(buy2)
  matchingEngine.acceptOrder(sell1)
  matchingEngine.acceptOrder(sell2)
  matchingEngine.acceptOrder(buy3)
  matchingEngine.acceptOrder(sell3)
  println(actualTrades)
  println(matchingEngine.buy.postings())
  println(matchingEngine.sell.postings())
}
