package chatless.db
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.MongoConversionHelper

import org.bson.{Transformer, BSON}

trait NumericConversionSerializers extends MongoConversionHelper {

  private val encodableBigInt = classOf[BigInt]

  private val transformer = new Transformer {
    log.trace("Encoding a JodaDateTime DateTime.")

    def transform(o: AnyRef): AnyRef = o match {
      case b: BigInt => b.toLong: java.lang.Long
      case _ => o
    }

  }

  override def register() {
    log.debug("registering extra numeric conversions")

    BSON.addEncodingHook(encodableBigInt, transformer)

    super.register()
  }
  override def unregister() {
    BSON.removeEncodingHooks(encodableBigInt)
    super.unregister()
  }
}

object RegisterNumericConversions extends NumericConversionSerializers {
  def apply() {
    super.register()
  }
}

object UnregisterNumericConversions extends NumericConversionSerializers {
  def apply() {
    super.unregister()
  }
}