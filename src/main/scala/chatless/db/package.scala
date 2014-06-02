package chatless

import scalaz._

package object db {
  type DbResult[+A] = DbError \/ A
}
