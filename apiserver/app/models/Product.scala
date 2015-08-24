package models

import scalikejdbc._
import org.joda.time.DateTime

/**
 * Created by nam.nvt on 7/14/2015.
 */
case class Product (
  prodId: Long,
  prodName: String,
  prodDescription: Option[String] = None,
  createdAt: DateTime,
  deletedAt: Option[DateTime] = None) {

    def save()(implicit session: DBSession = Product.autoSession): Product = Product.save(this)(session)
    def destroy()(implicit session: DBSession = Product.autoSession): Unit = Product.destroy(prodId)(session)

}
object Product extends SQLSyntaxSupport[Product] {

  def apply(prod: SyntaxProvider[Product])(rs: WrappedResultSet): Product = apply(prod.resultName)(rs)
  def apply(prod: ResultName[Product])(rs: WrappedResultSet): Product = new Product(
    prodId = rs.get(prod.prodId),
    prodName = rs.get(prod.prodName),
    prodDescription = rs.get(prod.prodDescription),
    createdAt = rs.get(prod.createdAt),
    deletedAt = rs.get(prod.deletedAt)
  )

  val prod = Product.syntax("prod")
  private val isNotDeleted = sqls.isNull(prod.deletedAt)

  def create(prodName: String, prodDescription: Option[String] = None, createdAt: DateTime = DateTime.now)(implicit session: DBSession = autoSession): Product = {
    val id = withSQL {
      insert.into(Product).namedValues(
        column.prodName -> prodName,
        column.prodDescription -> prodDescription,
        column.createdAt -> createdAt)
    }.updateAndReturnGeneratedKey.apply()

    Product(prodId = id, prodName = prodName, prodDescription = prodDescription, createdAt = createdAt)
  }
  def findById(id: Long)(implicit session: DBSession = autoSession): Option[Product] = withSQL {
    select.from(Product as prod).where.eq(prod.prodId, id).and.append(isNotDeleted)
  }.map(Product(prod)).single.apply()

  def save(m: Product)(implicit session: DBSession = autoSession): Product = {
    withSQL {
      update(Product).set(
        column.prodName -> m.prodName,
        column.prodDescription -> m.prodDescription
      ).where.eq(column.prodId, m.prodId).and.isNull(column.deletedAt)
    }.update.apply()
    m
  }

  def destroy(id: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    update(Product).set(column.deletedAt -> DateTime.now).where.eq(column.prodId, id)
  }.update.apply()
}
