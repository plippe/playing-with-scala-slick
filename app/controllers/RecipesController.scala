package controllers

import javax.inject.Inject
import java.util.UUID
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.libs.json.Json

class RecipesController @Inject()(
  val controllerComponents: ControllerComponents
) extends BaseController {

  val store = collection.mutable.Map.empty[UUID, models.Recipe]

  def get = Action {
    val models = store.values
    val json = Json.toJson(models)

    Ok(json)
  }

  def getById(id: UUID) = Action {
    store.get(id)
      .map(Json.toJson[models.Recipe])
      .fold(NotFound(s"Recipe not found: ${id}"))(Ok(_))
  }

  val missingContentType = UnprocessableEntity("Expected 'Content-Type' set to 'application/json'")
  val missingRecipeForm = UnprocessableEntity("Expected content to contain a recipe form")

  def post = Action { req =>
    req.body.asJson
      .toRight(missingContentType)
      .flatMap(_.asOpt[models.RecipeForm].toRight(missingRecipeForm))
      .map { form =>
        val model = models.Recipe.fromForm(form)

        store.update(model.id, model)
        val json = Json.toJson(model)
        Created(json)
      }
      .merge
  }

  def putById(id: UUID) = Action { req =>
    req.body.asJson
      .toRight(missingContentType)
      .flatMap(_.asOpt[models.RecipeForm].toRight(missingRecipeForm))
      .flatMap { form =>
        store.get(id)
          .toRight(NotFound(s"Recipe not found: ${id}"))
          .map((_, form))
      }
      .map { case (found, form) =>
        val model = models.Recipe.updated(found)(form)
        store.update(id, model)

        NoContent
      }
      .merge
  }

  def deleteById(id: UUID) = Action {
    store.get(id)
      .fold(NotFound(s"Recipe not found: ${id}")) { _ =>
        store.remove(id)

        NoContent
      }
  }
}
