package delight
package littlered

import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import Gens._

object HeaderProp {

  def properties: Prop =
    Prop.forAll(Gen.asciiStr, genListOfRecordedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        def resultsLengthProp = (results.length >= 1) :| s"results should always include header: ${results}"

        def resultsContainSuiteNameProp = (results(0) match {
          case Line(line) => line.contains(suiteName) :| s"${results} did not contain suiteName: ${suiteName}"
          case other => false :| s"expected Line but got: $other"
        })

        resultsLengthProp &&
        resultsContainSuiteNameProp
    }
}