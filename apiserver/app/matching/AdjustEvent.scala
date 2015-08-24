package matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:03 PM
 */

sealed trait AdjustEvent

case class Match(buyingBroker: String, sellingBroker: String,
                 price: Double, qty: Double) extends AdjustEvent