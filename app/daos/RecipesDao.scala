package daos

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import play.api.db.slick._
import scala.concurrent.Future
import slick.jdbc.{JdbcProfile, PostgresProfile, GetResult}

import helpers.slick.dbioaction._
import helpers.slick.jdbc._
import models.Recipe

@ImplementedBy(classOf[RecipesDaoSlickPlainSql])
trait RecipesDao {
  def findAll(): Future[Seq[Recipe]]
  def findById(id: UUID): Future[Option[Recipe]]
  def insert(recipe: Recipe): Future[Unit]
  def update(recipe: Recipe): Future[Unit]
  def delete(recipe: Recipe): Future[Unit]
}

@Singleton
class RecipesDaoMap extends RecipesDao {
  val map = collection.mutable.Map.empty[UUID, models.Recipe]

  def findAll(): Future[Seq[Recipe]] = Future.successful(map.values.toSeq)
  def findById(id: UUID): Future[Option[Recipe]] = Future.successful(map.get(id))
  def insert(recipe: Recipe): Future[Unit] = Future.successful(map.update(recipe.id, recipe))
  def update(recipe: Recipe): Future[Unit] = Future.successful(map.update(recipe.id, recipe))
  def delete(recipe: Recipe): Future[Unit] = Future.successful(map.remove(recipe.id))
}

class RecipesDaoSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] with RecipesDao {
  import profile.api._

  private val table = TableQuery[RecipesTable]
  private class RecipesTable(tag: Tag) extends Table[Recipe](tag, "recipes") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def description = column[String]("description")
    def createdAt = column[LocalDateTime]("created_at")
    def updatedAt = column[LocalDateTime]("updated_at")

    def * = (id, name, description, createdAt, updatedAt).mapTo[Recipe]
  }

  def findAll(): Future[Seq[Recipe]] = db.run(table.result)
  def findById(id: UUID): Future[Option[Recipe]] = db.run(table.filter(_.id === id).result.headOption)
  def insert(recipe: Recipe): Future[Unit] = db.run((table += recipe).void)
  def update(recipe: Recipe): Future[Unit] = db.run(table.filter(_.id === recipe.id).update(recipe).void)
  def delete(recipe: Recipe): Future[Unit] = db.run(table.filter(_.id === recipe.id).delete.void)
}

class RecipesDaoSlickPlainSql @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[PostgresProfile] with RecipesDao {
  import profile.api._

  implicit val getRecipeResult = GetResult(r => Recipe(r.<<, r.<<, r.<<, r.<<, r.<<))

  def findAll(): Future[Seq[Recipe]] = db.run(sql"""
    select id, name, description, created_at, updated_at
    from recipes
  """.as[Recipe])

  def findById(id: UUID): Future[Option[Recipe]] = db.run(sql"""
    select id, name, description, created_at, updated_at
    from recipes
    where id = ${id}
  """.as[Recipe].headOption)

  def insert(recipe: Recipe): Future[Unit] = db.run(sqlu"""
    insert into recipes(id, name, description, created_at, updated_at)
    values (${recipe.id}, ${recipe.name}, ${recipe.description}, ${recipe.createdAt}, ${recipe.updatedAt})
  """.void)

  def update(recipe: Recipe): Future[Unit] = db.run(sqlu"""
    update recipes
    set name = ${recipe.name},
      description = ${recipe.description},
      created_at = ${recipe.createdAt},
      updated_at = ${recipe.updatedAt}
    where id = ${recipe.id}
  """.void)

  def delete(recipe: Recipe): Future[Unit] = db.run(sqlu"""
    delete from recipes
    where id = ${recipe.id}
  """.void)
}
