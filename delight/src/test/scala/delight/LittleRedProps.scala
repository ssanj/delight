package delight

import org.scalacheck.Properties
import delight.littlered.AnyTest
import delight.littlered.PassedTest
import delight.littlered.FailedTest
import delight.littlered.WithStackTrace

object LittleRedProps extends Properties("LittleRed") {

  property("all tests output suiteName") = AnyTest.properties

  property("passed test properties")     = PassedTest.properties

  property("failed test properties")     = FailedTest.properties

  property("stacktrace properties")      = WithStackTrace.properties

}