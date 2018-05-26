package delight
package nature

import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import PropUtil._

object PassedOrFailedTest {

  private val padding = "  - "

  def properties(status: TestStatus): Prop =
    naturePassedOrFailed(ColourStatus.gen(status), ({ (events, lines) =>
      startWithPadding(lines)(padding) &&
      passedLineFormatting(events, lines, status)
    }))

  private def passedLineFormatting(events: Seq[RecordedEvent], lines: Seq[String], status: TestStatus): Prop =
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
          coloured("", ColourStatus.colour(status), ColourStatus.name(status))(sections(1).drop(1).dropRight(1))
        )
    }

  def naturePassedOrFailed(gen: Gen[List[RecordedEvent]], propertyAssertions: (Seq[RecordedEvent], Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], gen) {
      case (suiteName, events) =>
        val reporter = new Nature
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case Line(line) => line
        }

        propertyAssertions(events, lines)
      }
}