package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object FailedTest {

  private val padding = "  - "

  def properties: Prop =
    littleRedFailed { (events, lines) =>
      startWithPadding(lines) &&
      nameShouldBeRed(events, lines) &&
      endWithColourReset(lines) &&
      outputShouldHaveLengthOfParts(events, lines) &&
      onlyHaveOneFailedTest(events, lines)
    }

  private def startWithPadding(lines: Seq[String]): Prop =
    lines.map(line => line.startsWith(padding) :| s"Line: [${line}] does not start with [${padding}]")

  private def endWithColourReset(lines: Seq[String]): Prop =
    lines.map(line => line.endsWith(Colours.reset) :| s"Line: [${line}] does not end with colour reset")

  private def nameShouldBeRed(events: Seq[RecordedEvent] ,lines: Seq[String]): Prop =
    events.zip(lines).map {
      case (event, line) =>
        line.contains(s"${Colours.red}${event.testName}") :|
        s"Line: [${line}] does not have testName: ${event.testName} following red colour code"
    }

  private def outputShouldHaveLengthOfParts(events: Seq[RecordedEvent], lines: Seq[String]): Prop =
    events.zip(lines).map {
      case (event, line) =>
        val lineStructure = padding + Colours.red + event.testName + Colours.reset
        (lineStructure.length ?= line.length) :|
          s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
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
