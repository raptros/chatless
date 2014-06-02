package chatless


package object ops {
  import scalaz._
  type OperationResult[A] = OperationFailure \/ A

}
