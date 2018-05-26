package delight

import org.scalacheck.Properties
import delight.nature.HeaderTest
import delight.nature.PassedOrFailedTest
// import delight.nature.WithStackTrace

object NatureCommonProps extends Properties("NatureCommon") {

  property("header line") = HeaderTest.properties

  property("passed line") = PassedOrFailedTest.properties(Passed)

  property("failed line") = PassedOrFailedTest.properties(Failed)

  // property("stacktrace properties")      = WithStackTrace.properties

}