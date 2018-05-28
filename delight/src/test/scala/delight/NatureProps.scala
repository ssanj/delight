package delight

import org.scalacheck.Properties
import delight.nature.HeaderProp
import delight.nature.PassedProp
import delight.nature.FailedProp

object NatureProps extends Properties("Nature") {

  property("header line") = HeaderProp.properties

  property("passed line") = PassedProp.properties

  property("failed line") = FailedProp.properties(FailedProp.WithoutStackTrace)

  property("failed line with stacktrace") = FailedProp.properties(FailedProp.WithStackTrace)
}