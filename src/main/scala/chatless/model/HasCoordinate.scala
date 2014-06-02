package chatless.model

trait HasCoordinate[+C <: Coordinate] {
  def coordinate: C
}
