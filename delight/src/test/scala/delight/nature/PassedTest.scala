package delight
package nature

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object PassedTest {

  private val padding = "  - "

  def properties: Prop =
    naturePassed { (events, lines) =>
      startWithPadding(lines) &&
      passedLineFormatting(events, lines)
    }

  private def coloured(prefix: String, colour: String, text: String)(section: String): Prop = {
    Prop.all(
      section.startsWith(prefix) :| s"Section: [${section}] does not start with prefix: ${prefix}",
      section.stripPrefix(prefix).startsWith(colour)  :| s"Section: [${section}] does have ${colour}colour${Colours.reset} following prefix",
      section.stripSuffix(Colours.reset).endsWith(text) :| s"Section: [${section}] does not have text: [${text}] before reset",
      section.endsWith(Colours.reset) :| s"Section: [${section}] does not end with colour reset"
    )
  }

  private def startWithPadding(lines: Seq[String]): Prop =
    lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")

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