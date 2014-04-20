package chatless.model

trait HasCoordinate[T <: Coordinate] {
  def coordinate: T
}
