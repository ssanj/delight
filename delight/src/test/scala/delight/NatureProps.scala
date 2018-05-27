package delight

import org.scalacheck.Properties
import delight.nature.HeaderProp
import delight.nature.PassedProp
import delight.nature.FailedProp
// import delight.nature.WithStackTrace

object NatureProps extends Properties("Nature") {

  property("header line") = HeaderProp.properties

  property("passed line") = PassedProp.properties

  property("failed line") = FailedProp.properties

  // property("stacktrace properties")      = WithStackTrace.properties

}