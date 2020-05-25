package helpers.slick

import java.sql.{Timestamp, Types}
import java.time.LocalDateTime
import java.util.UUID
import _root_.slick.jdbc._

trait JdbcSyntax {

  implicit final def helpersSlickGetResultLocalDateTime: GetResult[LocalDateTime] =
    GetResult(r => r.nextTimestamp.toLocalDateTime)

  implicit final def helpersSlickSetParameterLocalDateTime: SetParameter[LocalDateTime] =
    SetParameter { case (v, pp) => pp.setTimestamp(Timestamp.valueOf(v)) }

  implicit final def helpersSlickGetResultUUID: GetResult[UUID] =
    GetResult(r => r.nextObject.asInstanceOf[UUID])

  implicit final def helpersSlickSetParameterUUID: SetParameter[UUID] =
    SetParameter { case (v, pp) => pp.setObject(v, Types.OTHER) }

}

object jdbc extends JdbcSyntax
