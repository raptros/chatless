package chatless.op2
import chatless._
import argonaut.Json

case class TopicM(
  tid:TopicId,
  title:String,
  public:Boolean,
  info:Json,
  op:UserId,
  sops:Set[UserId],
  participating:Set[UserId],
  tags:List[String]) {

}
