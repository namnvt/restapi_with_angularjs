package matching

/**
 * User: namnvt
 * Date: 08/24/2015
 * Time: 05:00 PM
 */

sealed trait AdjustEvent

case class Match(buyingBroker: String, sellingBroker: String,
                 price: Double, qty: Double) extends AdjustEvent
