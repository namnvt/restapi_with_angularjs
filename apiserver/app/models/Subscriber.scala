package models

import scalikejdbc._
import org.joda.time.DateTime

/**
 * Created by nam.nvt on 6/30/2015.
 */
case class Subscriber(
                    id: Long,
                    username: String,
                    email: Option[String] = None,
                    password: String,
                    createdAt: DateTime,
                    deletedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Subscriber.autoSession): Subscriber = Subscriber.save(this)(session)
  def destroy()(implicit session: DBSession = Subscriber.autoSession): Unit = Subscriber.destroy(id)(session)
}
object Subscriber extends SQLSyntaxSupport[Subscriber] {

  def apply(sc: SyntaxProvider[Subscriber])(rs: WrappedResultSet): Subscriber = apply(sc.resultName)(rs)
  def apply(sc: ResultName[Subscriber])(rs: WrappedResultSet): Subscriber = new Subscriber(
    id = rs.get(sc.id),
    username = rs.get(sc.username),
    email = rs.get(sc.email),
    password = rs.get(sc.password),
    createdAt = rs.get(sc.createdAt),
    deletedAt = rs.get(sc.deletedAt)
  )

  val sc = Subscriber.syntax("sc")
  private val isNotDeleted = sqls.isNull(sc.deletedAt)

  def create(username: String, email: Option[String] = None, password: String, createdAt: DateTime = DateTime.now)(implicit session: DBSession = autoSession): Subscriber = {
    val id = withSQL {
      insert.into(Subscriber).namedValues(
        column.username -> username,
        column.email -> email,
        column.password -> password,
        column.createdAt -> createdAt)
    }.updateAndReturnGeneratedKey.apply()

    Subscriber(id = id, username = username, email = email, password = password, createdAt = createdAt)
  }
  
  def save(m: Subscriber)(implicit session: DBSession = autoSession): Subscriber = {
    withSQL {
      update(Subscriber).set(
        column.username -> m.username, 
        column.email -> m.email,
        column.password -> m.password
      ).where.eq(column.id, m.id).and.isNull(column.deletedAt)
    }.update.apply()
    m
  }

  def destroy(id: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    update(Subscriber).set(column.deletedAt -> DateTime.now).where.eq(column.id, id)
  }.update.apply()
}
