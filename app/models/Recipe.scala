package models

import java.time.LocalDateTime
import java.util.UUID
import play.api.libs.json.Json

case class Recipe(
  id: UUID,
  name: String,
  description: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime,
)

object Recipe {
  implicit def recipePlayJsonWrites = Json.writes[Recipe]

  def fromForm(form: RecipeForm): Recipe =
    Recipe(
      id = UUID.randomUUID,
      name = form.name,
      description = form.description,
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now(),
    )

  def updated(self: Recipe)(form: RecipeForm): Recipe =
    self.copy(
      name = form.name,
      description = form.description,
      updatedAt = LocalDateTime.now(),
    )

  def tupled(tuple: ((UUID, String, String, LocalDateTime, LocalDateTime))): Recipe =
    (apply _).tupled(tuple)
}

case class RecipeForm(
  name: String,
  description: String,
)

object RecipeForm {
  implicit def recipeFormPlayJsonReads = Json.reads[RecipeForm]

  def fromModel(model: Recipe): RecipeForm =
    RecipeForm(
      name = model.name,
      description = model.description,
    )
}
