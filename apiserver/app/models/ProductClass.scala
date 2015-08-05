package models

import scalikejdbc._
import org.joda.time.DateTime

/**
 * Created by nam.nvt on 7/14/2015.
 */
case class ProductClass (
                     prodClsId: Long,
                     prodId: Long,
                     prodClsName: String,
                     prodClsDesc: Option[String] = None,
                     createdAt: DateTime,
                     deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = ProductClass.autoSession): ProductClass = ProductClass.save(this)(session)
  def destroy()(implicit session: DBSession = ProductClass.autoSession): Unit = ProductClass.destroy(prodClsId)(session)

}
object ProductClass extends SQLSyntaxSupport[ProductClass] {

  def apply(prodcls: SyntaxProvider[ProductClass])(rs: WrappedResultSet): ProductClass = apply(prodcls.resultName)(rs)
  def apply(prodcls: ResultName[ProductClass])(rs: WrappedResultSet): ProductClass = new ProductClass(
    prodClsId = rs.get(prodcls.prodClsId),
    prodId = rs.get(prodcls.prodId),
    prodClsName = rs.get(prodcls.prodClsName),
    prodClsDesc = rs.get(prodcls.prodClsDesc),
    createdAt = rs.get(prodcls.createdAt),
    deletedAt = rs.get(prodcls.deletedAt)
  )

  val prodcls = ProductClass.syntax("prodcls")
  private val isNotDeleted = sqls.isNull(prodcls.deletedAt)

  def create(prodClsName: String, prodId: Long, prodClsDesc: Option[String] = None, createdAt: DateTime = DateTime.now)(implicit session: DBSession = autoSession): ProductClass = {
    val id = withSQL {
      insert.into(ProductClass).namedValues(
        column.prodId -> prodId,
        column.prodClsName -> prodClsName,
        column.prodClsDesc -> prodClsDesc,
        column.createdAt -> createdAt)
    }.updateAndReturnGeneratedKey.apply()

    ProductClass(prodClsId = id, prodId = prodId, prodClsName = prodClsName, prodClsDesc = prodClsDesc, createdAt = createdAt)
  }
  def findById(id: Long)(implicit session: DBSession = autoSession): Option[ProductClass] = withSQL {
    select.from(ProductClass as prodcls).where.eq(prodcls.prodClsId, id).and.append(isNotDeleted)
  }.map(ProductClass(prodcls)).single.apply()

  def save(m: ProductClass)(implicit session: DBSession = autoSession): ProductClass = {
    withSQL {
      update(ProductClass).set(
        column.prodClsName -> m.prodClsName,
        column.prodClsDesc -> m.prodClsDesc
      ).where.eq(column.prodClsId, m.prodClsId).and.isNull(column.deletedAt)
    }.update.apply()
    m
  }

  def destroy(id: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    update(ProductClass).set(column.deletedAt -> DateTime.now).where.eq(column.prodClsId, id)
  }.update.apply()
}
