package daos

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.db.slick._
import scala.concurrent.Future
import slick.jdbc.{JdbcProfile, GetResult}

import helpers.slick.dbioaction._
import helpers.slick.jdbc._

trait RecipesDao {
  def findAll(): Future[Seq[models.Recipe]]
  def findById(id: UUID): Future[Option[models.Recipe]]
  def insert(recipe: models.Recipe): Future[Unit]
  def update(recipe: models.Recipe): Future[Unit]
  def delete(recipe: models.Recipe): Future[Unit]
}

@Singleton
class RecipesDaoMap extends RecipesDao {
  val map = collection.mutable.Map.empty[UUID, models.Recipe]

  def findAll(): Future[Seq[models.Recipe]] = Future.successful(map.values.toSeq)
  def findById(id: UUID): Future[Option[models.Recipe]] = Future.successful(map.get(id))
  def insert(recipe: models.Recipe): Future[Unit] = Future.successful(map.update(recipe.id, recipe))
  def update(recipe: models.Recipe): Future[Unit] = Future.successful(map.update(recipe.id, recipe))
  def delete(recipe: models.Recipe): Future[Unit] = Future.successful(map.remove(recipe.id))
}

class RecipesDaoSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends RecipesDao with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val table = TableQuery[RecipesTable]
  private class RecipesTable(tag: Tag) extends Table[models.Recipe](tag, "recipes") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def description = column[String]("description")
    def createdAt = column[LocalDateTime]("created_at")
    def updatedAt = column[LocalDateTime]("updated_at")

    def * = (id, name, description, createdAt, updatedAt).mapTo[models.Recipe]
  }

  def findAll(): Future[Seq[models.Recipe]] = db.run(table.result)
  def findById(id: UUID): Future[Option[models.Recipe]] = db.run(table.filter(_.id === id).result.headOption)
  def insert(recipe: models.Recipe): Future[Unit] = db.run((table += recipe).void)
  def update(recipe: models.Recipe): Future[Unit] = db.run(table.filter(_.id === recipe.id).update(recipe).void)
  def delete(recipe: models.Recipe): Future[Unit] = db.run(table.filter(_.id === recipe.id).delete.void)
}

class RecipesDaoSlickPlainSql @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends RecipesDao with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  implicit val getRecipeResult: GetResult[models.Recipe] = GetResult(r => models.Recipe(r.<<, r.<<, r.<<, r.<<, r.<<))

  def findAll(): Future[Seq[models.Recipe]] = db.run(sql"""
    select id, name, description, created_at, updated_at
    from recipes
  """.as[models.Recipe])

  def findById(id: UUID): Future[Option[models.Recipe]] = db.run(sql"""
    select id, name, description, created_at, updated_at
    from recipes
    where id = ${id}
  """.as[models.Recipe].headOption)

  def insert(recipe: models.Recipe): Future[Unit] = db.run(sqlu"""
    insert into recipes(id, name, description, created_at, updated_at)
    values (${recipe.id}, ${recipe.name}, ${recipe.description}, ${recipe.createdAt}, ${recipe.updatedAt})
  """.void)

  def update(recipe: models.Recipe): Future[Unit] = db.run(sqlu"""
    update recipes
    set name = ${recipe.name},
      description = ${recipe.description},
      created_at = ${recipe.createdAt},
      updated_at = ${recipe.updatedAt}
    where id = ${recipe.id}
  """.void)

  def delete(recipe: models.Recipe): Future[Unit] = db.run(sqlu"""
    delete from recipes
    where id = ${recipe.id}
  """.void)
}
