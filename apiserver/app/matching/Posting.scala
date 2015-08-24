package matching

import org.joda.time.DateTime



/**
 * @author nam.nvt
 */
case class Posting (var trader:Trader, var side:Side, var quantity:Double, var price:Double, var createdAt: DateTime, var expireAt: DateTime)

