package helpers.slick

import _root_.slick.dbio._

final class DBIOActionOps[+R, +S <: NoStream, -E <: Effect](val self: DBIOAction[R, S, E]) extends AnyVal {
  def as[R2](newValue: => R2): DBIOAction[R2, NoStream, E with Effect] = self.andThen(slick.dbio.DBIOAction.successful(newValue))
  def void() = as(())
}

trait DBIOActionSyntax {
  implicit final def helpersSlickDBIOActionOps[R, S <: NoStream, E <: Effect](self: DBIOAction[R, S, E]): DBIOActionOps[R, S, E] =
    new DBIOActionOps[R, S, E](self)
}

object dbioaction extends DBIOActionSyntax
