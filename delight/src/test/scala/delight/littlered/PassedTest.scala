package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._

object PassedTest {

  private val padding             = "  - "

  def properties: Prop =
    littleRedPassed { (events, lines) =>
      startWithPadding(lines) &&
      allLinesContainTestName(events, lines) &&
      nameShouldBeGreenColour(events, lines) &&
      endWithColourReset(lines) &&
      outputShouldHaveLengthOfParts(events, lines)
    }

  private def startWithPadding(lines: Seq[String]): Prop = {
    val props = lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")
    Prop.all(props:_*)
  }

  private def endWithColourReset(lines: Seq[String]): Prop = {
    val props = lines.map(l => l.endsWith(Colours.reset) :| s"Line: [${l}] does not end with colour reset")
    Prop.all(props:_*)
  }

  private def nameShouldBeGreenColour(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
    val props = events.zip(lines).map {
      case (event, line) => line.contains(s"${Colours.green}${event.testName}") :|
        s"Line: [${line}] does not have testName: ${event.testName} following green colour code"
    }

    Prop.all(props:_*)
  }

  private def outputShouldHaveLengthOfParts(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
    val props = events.zip(lines).map {
      case (event, line) =>
        val lineStructure = padding + Colours.green + event.testName + Colours.reset
        (lineStructure.length ?= line.length) :|
          s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
    }

    Prop.all(props:_*)
  }

  private def allLinesContainTestName(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
    val props = lines.zip(events).map {
      case (line, event) => line.contains(event.testName) :| s"Line:$line doesn't contain testName:${event.testName}"
    }

    Prop.all(props:_*)
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