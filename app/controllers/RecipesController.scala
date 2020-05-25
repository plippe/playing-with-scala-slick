package controllers

import javax.inject.Inject
import java.util.UUID
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

class RecipesController @Inject()(
  dao: daos.RecipesDao,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

  def get = Action.async {
    dao.findAll()
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def getById(id: UUID) = Action.async {
    dao.findById(id)
      .map { optModel =>
        optModel
          .map(Json.toJson[models.Recipe])
          .fold(NotFound(s"Recipe not found: ${id}"))(Ok(_))
      }
  }

  val missingContentType = Future.successful(UnprocessableEntity("Expected 'Content-Type' set to 'application/json'"))
  val missingRecipeForm = Future.successful(UnprocessableEntity("Expected content to contain a recipe form"))

  def post = Action.async { req =>
    req.body.asJson
      .toRight(missingContentType)
      .flatMap(_.asOpt[models.RecipeForm].toRight(missingRecipeForm))
      .map { form =>
        val model = models.Recipe.fromForm(form)

        dao.insert(model)
          .map { _ =>
            val json = Json.toJson(model)
            Created(json)
          }
      }
      .merge
  }

  def putById(id: UUID) = Action.async { req =>
    req.body.asJson
      .toRight(missingContentType)
      .flatMap(_.asOpt[models.RecipeForm].toRight(missingRecipeForm))
      .map { form =>
        dao.findById(id)
          .flatMap {
            case None => Future.successful((NotFound(s"Recipe not found: ${id}")))
            case Some(found) =>
              val model = models.Recipe.updated(found)(form)
              dao.update(model).map(_ => NoContent)
          }
      }
      .merge
  }

  def deleteById(id: UUID) = Action.async {
    dao.findById(id)
      .flatMap {
        case None => Future.successful((NotFound(s"Recipe not found: ${id}")))
        case Some(found) => dao.delete(found).map(_ => NoContent)
      }
  }
}
