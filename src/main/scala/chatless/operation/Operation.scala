package chatless.operation

import chatless.UserId
import argonaut._
import Argonaut._

case class Operation(cid:UserId, res:OpRes, spec:OpSpec)

object Operation {
  implicit def OperationEncodeJ:EncodeJson[Operation] = jencode3L { op:Operation =>
    (op.cid, op.res, op.spec)
  } ("cid", "res", "spec")

  implicit def JDecodeOperation:DecodeJson[Operation] = jdecode3L { Operation.apply } ("cid", "res", "spec")
}






