package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object FailedProp {

  private val padding = "  - "

  def properties: Prop =
    littleRedFailed { (events, lines) =>
      startWithPadding(lines)(padding) &&
      formattedFailedLine(events, lines) &&
      onlyHaveOneFailedTest(events, lines)
    }

  private def formattedFailedLine(events: Seq[RecordedEvent] ,lines: Seq[String]): Prop =
    events.zip(lines).map {
      case (event, line) => coloured(padding, Colours.red, event.testName)(line)
    }

  private def onlyHaveOneFailedTest(events: Seq[RecordedEvent] ,lines: Seq[String]): Prop = {
    //even if there are more than one failed test, there should be only one displayed
    if (events.length >= 1) (lines.length == 1) :| "There should be only one failed test"
    else true
  }

  private def littleRedFailed(propertyAssertions: (Seq[RecordedEvent], Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedFailedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case MultiLine(Line(line), _, _*) => line
          case Line(line) => line
        }

        propertyAssertions(events, lines)
      }
}
