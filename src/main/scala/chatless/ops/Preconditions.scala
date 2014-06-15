package chatless.ops

import spray.http.{StatusCodes, StatusCode}

object Preconditions {
  sealed class Precondition(val statusCode: StatusCode)

  case object READ_DENIED
    extends Precondition(StatusCodes.Forbidden)

  case object USER_ALREADY_MEMBER
    extends Precondition(StatusCodes.BadRequest)

  case object USER_NOT_MEMBER
    extends Precondition(StatusCodes.BadRequest)

  case object SET_MEMBER_DENIED
    extends Precondition(StatusCodes.Forbidden)

  case object WRITE_DENIED
    extends Precondition(StatusCodes.Forbidden)

  case object INVITE_DENIED
    extends Precondition(StatusCodes.Forbidden)

  case object USER_NOT_LOCAL
    extends Precondition(StatusCodes.InternalServerError)
}
