package delight

import org.scalacheck.Properties
import delight.littlered.HeaderProp
import delight.littlered.PassedProp
import delight.littlered.FailedProp
import delight.littlered.WithStackTrace

object LittleRedProps extends Properties("LittleRed") {

  property("header line")     = HeaderProp.properties

  property("passed line")     = PassedProp.properties

  property("failed line")     = FailedProp.properties

  property("stacktrace line") = WithStackTrace.properties

}