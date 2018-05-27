package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object PassedProp {

  private val padding = "  - "

  def properties: Prop =
    littleRedPassed { (events, lines) =>
      startWithPadding(lines)(padding) &&
      formattedPassedLine(events, lines)
    }

  private def formattedPassedLine(events: Seq[RecordedEvent], lines: Seq[String]): Prop =
    events.zip(lines).map {
      case (event, line) => coloured(padding, Colours.green, event.testName)(line)
    }

  private def littleRedPassed(propertyAssertions: (Seq[RecordedEvent], Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedPassedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case Line(line) => line
        }

        propertyAssertions(events, lines)
      }
}