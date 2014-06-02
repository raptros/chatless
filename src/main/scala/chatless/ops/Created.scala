package chatless.ops

import chatless.model.{Coordinate, HasCoordinate}

case class Created[A <: HasCoordinate[Coordinate]](a: A) {
  def coordinate = a.coordinate
}
