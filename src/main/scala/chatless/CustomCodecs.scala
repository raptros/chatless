package chatless
import chatless.db._
import argonaut._
import Argonaut._
import scala.reflect.runtime.universe._
import scala.reflect.api.Universe
import scalaz.syntax.id._
import scalaz._
import scalaz.Functor._

object CustomCodecs {
  /*
  private def OpRes2JV(iFormats:Formats)(or:OpRes):JValue = or match {
    case ResUser(uid) => pair2Assoc("res" -> "user") ~ ("uid" -> uid)
    case ResTopic(tid) => pair2Assoc("res" -> "topic") ~ ("tid" -> tid)
  }

  private def pfOR2JV(iFormats:Formats):PartialFunction[Any, JValue] = {
    case or:OpRes => OpRes2JV(iFormats)(or)
  }

  private def jv2OR(iFormats:Formats):PartialFunction[JValue, OpRes] = {
    case JObject(JField("res", JString("user")) :: JField("uid", JString(uid)) :: Nil) => ResUser(uid)
    case JObject(JField("res", JString("topic")) :: JField("tid", JString(tid)) :: Nil) => ResTopic(tid)
  }

  class OpResSerializer extends CustomSerializer[OpRes]( formats => jv2OR(formats) -> pfOR2JV(formats) )

  private def OpSpec2JV(iFormats:Formats)(os:OpSpec):JValue = os match {
    case GetAll => { "op" -> "get" } ~ { "allfields" -> true }
    case GetFields(fields @ _* ) => {"op" -> "get"} ~ { "fields" -> fields }
    case ReplaceField(field, newVal) => { "op" -> "update" } ~ { "spec" -> "replace" } ~ { "value" -> newVal.toString }
    case AppendToList(field, newVal) => { "op" -> "update" } ~ {"spec" -> "append" } ~ { "value" -> newVal.toString }
    case DeleteFromList(field, oldVal) => { "op" -> "update" } ~ {"spec" -> "delete" } ~ { "oldVal" -> oldVal.toString }
  }

  private def pfOS2JV(iFormats:Formats):PartialFunction[Any, JValue] = {
    case os:OpSpec => OpSpec2JV(iFormats)(os)
  }

  private def jv2OS(iFormats:Formats):PartialFunction[JValue, OpSpec] = {
    case JObject(JField("op", JString("get")) :: JField("allfields", JBool(true)) :: Nil) => GetAll
    case JObject(JField("op", JString("get")) :: JField("fields", JArray(fields)) :: Nil) => GetFields(fields flatMap {
      case JString(s) => Some(s)
      case _ => None
    }: _*)
      //oh
    case JObject(JField("op", JString("update")) :: JField("spec", JString("replace")) :: JField("value", value) :: Nil) => GetAll
  }

  class OpResSerializer extends CustomSerializer[OpRes]( formats => jv2OR(formats) -> pfOR2JV(formats) )
  */
  type FEncode[A] = Json => EncodeJson[A]

  def fEncode[A](f:(Json, A) => Json):FEncode[A] = {j:Json => EncodeJson[A] { a => f(j,a) } }

  implicit class EncodeOps[A](ej:EncodeJson[A]) {
    val apEJ: A => Json = ej.apply _
    def flatMap(f: FEncode[A]):EncodeJson[A] = EncodeJson { a:A =>
      val j = ej(a)
      j deepmerge f(j)(a)
    }
    //def upConv(f: Json => A => Json):EncodeJson
    def map(f:Json => Json) = EncodeJson { f compose apEJ }
    def append(ej2:EncodeJson[A]) = EncodeJson { a:A => ej(a) deepmerge  ej2(a) }

  }

  implicit def OpSpecEncodeJ:EncodeJson[OpSpec] = EncodeJson { spec:OpSpec => spec.asJson }


  implicit def OpResEncodeJ:EncodeJson[OpRes] = EncodeJson { _.asJson }


  implicit def JDecodeOpRes:DecodeJson[OpRes] = DecodeJson { c => (c --\ "res").as[String] flatMap {
    case "user" => (c --\ "uid").as[String] map { uid => ResUser(uid) }
    case "topic" => (c --\ "tid").as[String] map { tid => ResTopic(tid) }
    case _ => DecodeResult.fail("not a valid resource spec", c.history)
  }}

/*  def JDecodeGetAll:DecodeJson[GetSpec] = DecodeJson { c =>
    (c --\ "allfields").as[Boolean] flatMap { allfields =>
      if (allfields) DecodeResult.okResult(GetAll) else DecodeResult.fail("nope", c.history)
    }
  }*/

  def JDecodeGetFields:DecodeJson[GetSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    fields <- (c --\ "fields").as[List[String]]
    res <- if (op == "get") okResult(GetFields(fields:_*)) else failResult("not a get all fields", c.history)
  } yield res }

  def JDecodeGetAll:DecodeJson[GetSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    allF <- (c --\ "allfields").as[Boolean]
    res <- if (op == "get" && allF) okResult(GetAll) else failResult("not a get all fields", c.history)
  } yield res }

  def JDecodeGetOp:DecodeJson[GetSpec] = JDecodeGetAll ||| JDecodeGetFields

  def JDecodeUpdateSpec1:DecodeJson[UpdateSpec[_]] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    spec <- (c --\ "spec").as[String]
    value <- (c --\ "value ").as[String]
  } yield (op, spec) match {
      case ("update", "replace") =>
    }
  }

  trait TaggedAndEncodable[A] extends TypeTag[A] with EncodeJson[A]

  implicit def toTaggedAndEncodable[A](implicit tt:TypeTag[A], ej:EncodeJson[A]):TaggedAndEncodable[A] = new TaggedAndEncodable[A] {
    def encode(a:A) = ej.encode(a)

    def in[U <: Universe with Singleton](otherMirror: reflect.api.Mirror[U]): U # TypeTag[A] = tt.in(otherMirror)

    def mirror = tt.mirror

    def tpe = tt.tpe
  }




//  def JDecodeUpdateSpec:

/*  implicit def JDecodeOpSpec:DecodeJson[OpSpec] = DecodeJson { c => for {
    op <- (c --\ "op").as[String]
    spec <- if ("op" == "get")
  }
  }*/

  implicit def OperationEncodeJ:EncodeJson[Operation] = jencode3L { op:Operation =>
    (op.cid, op.res, op.spec)
  } ("cid", "res", "spec")

  implicit def JDecodeOperation:DecodeJson[Operation] = jdecode3L { Operation.apply } ("cid", "res", "spec")

 //todo just put all the implementation stuff inside the classes they belong to



}
