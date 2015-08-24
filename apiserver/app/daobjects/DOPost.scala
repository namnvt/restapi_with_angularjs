package daobjects

import org.joda.time.DateTime
import matching.Side


/**
 * @author nam.nvt
 */
case class DOPost (var trader:DOTrader, var side:Side, var quantity:Int, var price:Double, var createdAt: DateTime, var expireAt: DateTime)

