package chatless.macros

import argonaut._
import Argonaut._

object JsonMacros {

  import scala.reflect.macros.blackbox.Context
  import scala.language.experimental.macros

  def deriveCaseCodecJson[C]: CodecJson[C] = macro deriveCaseCodecJsonImpl[C]

  def deriveCaseCodecJsonImpl[C: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[C]
    val decls = tpe.decls
    val ctor = decls collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    } getOrElse {
      throw new IllegalArgumentException(s"can't find primary constructor for ${tpe}!")
    }
    val params = ctor.paramLists.head
    val keyNames = params map { p => p.name.decodedName.toString}
    val count = params.length
    val paramTypes = params map { p => tq"${p.typeSignature}"}

    val codecDecls = weakTypeOf[CodecJsons]
    val targetMethod = codecDecls.members.find { s =>
      s.isMethod && (s.name.decodedName.toString == s"casecodec$count")
    } getOrElse {
      throw new IllegalArgumentException(s"could not find a casecodec method for args count $count")
    }

    val typeSym = tq"${tpe.typeSymbol}"
    val companion = tpe.typeSymbol.companion
    val implicitlies = paramTypes flatMap { t =>
      List(q"scala.Predef.implicitly[EncodeJson[$t]]", q"scala.Predef.implicitly[DecodeJson[$t]]")
    }

    q"""
        argonaut.Argonaut.$targetMethod[..$paramTypes, $typeSym]($companion.apply, $companion.unapply)(..$keyNames)(..$implicitlies)
     """
  }

  def deriveCaseEncodeJson[C]: EncodeJson[C] = macro deriveCaseEncodeJsonImpl[C]

  def deriveCaseEncodeJsonImpl[C: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[C]
    val decls = tpe.decls
    val ctor = decls collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    } getOrElse {
      throw new IllegalArgumentException(s"can't find primary constructor for ${tpe}!")
    }
    val params = ctor.paramLists.head
    val keyNames = params map { p => p.name.decodedName.toString}
    val count = params.length
    val paramTypes = params map { p => tq"${p.typeSignature}"}

    val codecDecls = weakTypeOf[EncodeJsons]
    val targetMethod = codecDecls.members.find { s =>
      s.isMethod && (s.name.decodedName.toString == s"jencode${count}L")
    } getOrElse {
      throw new IllegalArgumentException(s"could not find a jencode_L method for args count $count")
    }

    val typeSym = tq"${tpe.typeSymbol}"
    val companion = tpe.typeSymbol.companion
    val implicitlies = paramTypes map { t =>
      q"scala.Predef.implicitly[EncodeJson[$t]]"
    }

    q"""
        argonaut.Argonaut.$targetMethod[$typeSym, ..$paramTypes](($companion.unapply _) andThen (_.get))(..$keyNames)(..$implicitlies)
     """
  }
}
