package delight
package nature

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object PassedProp {

  private val padding = "  - "

  def properties: Prop =
    naturePassed{ (events, lines) =>
      startWithPadding(lines)(padding) &&
      passedLineFormatting(events, lines)
    }

  private def passedLineFormatting(events: Seq[RecordedEvent], lines: Seq[String]): Prop =
    events.zip(lines).map {
      case (event, line) =>
        val split = line.stripPrefix(padding).split(" ")
        //if the test name has spaces then join all of them
        val sections  = if (split.length > 2) Array(split.init.mkString(" "), split.last) else split
        Prop.all(
          (sections.length ?= 2) :| s"Expected 2 sections but got: ${sections.length}, ${sections.mkString(",")}",
          coloured("", Colours.cyan, event.testName)(sections(0)),
          sections(1).startsWith("[") :| s"Section does not start with '[': ${sections(1)}",
          sections(1).endsWith("]") :| s"Section does not end with '[': ${sections(1)}",
          coloured("", Colours.green, "PASSED")(sections(1).drop(1).dropRight(1))
        )
    }

  def naturePassed(propertyAssertions: (Seq[RecordedEvent], Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedPassedEvent) {
      case (suiteName, events) =>
        val reporter = new Nature
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case Line(line) => line
        }

        propertyAssertions(events, lines)
      }
}