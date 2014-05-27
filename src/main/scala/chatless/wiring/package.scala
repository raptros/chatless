package chatless

import com.google.inject.assistedinject.FactoryModuleBuilder

package object wiring {

  import net.codingwell.scalaguice.typeLiteral

  /*
  def FMB = new FactoryModuleBuilder()

  implicit class FactoryModuleBuilderWrapper(fmb: FactoryModuleBuilder) {
    def bindImpl[T : Manifest, TImpl <: T : Manifest] = fmb.implement(typeLiteral[T], typeLiteral[TImpl])

    def buildF[F: Manifest] = fmb.build(typeLiteral[F])
  }*/
}
