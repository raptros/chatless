package chatless

import chatless.operation.{JsonVC, StringVC, BooleanVC}


package object db {
  import argonaut._
  import Argonaut._

  import com.mongodb.casbah.Imports._
  import chatless.operation.ValueContainer

  def vc2DBO(field:String, vc:ValueContainer):MongoDBObject = vc match {
    case BooleanVC(contained) => map2MongoDBObject(Map(field -> contained))
    case StringVC(contained) => map2MongoDBObject(Map(field -> contained))
    case JsonVC(contained) => map2MongoDBObject(Map(field -> contained.nospaces))
  }

  implicit val aqpVC:AsQueryParam[ValueContainer] = new AsQueryParam[ValueContainer] {
    def asQueryParam(vc:ValueContainer) = vc match {
      case BooleanVC(contained) => AsQueryParam.boolean.asQueryParam(contained)
      case StringVC(contained) => AsQueryParam.string.asQueryParam(contained)
      case JsonVC(contained) => AsQueryParam.string.asQueryParam(contained.nospaces)
    }
  }
}
