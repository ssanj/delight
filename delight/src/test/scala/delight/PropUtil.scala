package delight

import scala.collection.GenTraversableOnce
import scala.language.implicitConversions
import org.scalacheck.Prop

object PropUtil {
  implicit def toProps(bools: GenTraversableOnce[Prop]): Prop = Prop.all(bools.toList:_*)
}