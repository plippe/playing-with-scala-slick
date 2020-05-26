import com.google.inject.AbstractModule

class Module extends AbstractModule {
  override def configure() =
    bind(classOf[daos.RecipesDao]).to(classOf[daos.RecipesDaoSlickPlainSql])
}
