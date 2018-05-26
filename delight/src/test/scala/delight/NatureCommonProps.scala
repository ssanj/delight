package delight

import org.scalacheck.Properties
// import delight.nature.AnyTest
import delight.nature.PassedOrFailedTest
// import delight.nature.FailedTest
// import delight.nature.WithStackTrace

object NatureCommonProps extends Properties("NatureCommon") {

  // property("all tests output suiteName") = AnyTest.properties

  property("passed test") = PassedOrFailedTest.properties(Passed)

  property("failed test") = PassedOrFailedTest.properties(Failed)

  // property("failed test properties")     = FailedTest.properties

  // property("stacktrace properties")      = WithStackTrace.properties

}