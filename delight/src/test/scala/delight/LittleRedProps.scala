package delight

import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._

object LittleRedProps extends Properties("LittleRed") {

  private val padding = "  - "

  property("always output suiteName") =
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

  property("passed test should contain test name") =
    littleRedPassed { (events, results, lines) =>
      def allLinesContainTestNameProp = {
          val props = lines.zip(events).map {
            case (line, event) => line.contains(event.testName) :| s"Line:$line doesn't contain testName:${event.testName}"
          }

          Prop.all(props:_*)
      }

      allLinesContainTestNameProp
    }

  property("passed test should start with padding") =
      littleRedPassed { (_, results, lines) =>
        val props = lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")
        Prop.all(props:_*)
      }

  property("passed test should end with console colour reset") =
      littleRedPassed { (_, results, lines) =>
        val props = lines.map(l => l.endsWith(Colours.reset) :| s"Line: [${l}] does not end with colour reset")
        Prop.all(props:_*)
      }

  property("passed test have green colour code") =
      littleRedPassed { (_, results, lines) =>
        val props = lines.map(l => l.contains(Colours.green) :| s"Line: [${l}] does not contain colour green")
        Prop.all(props:_*)
      }

  property("passed test name should follow green colour code") =
      littleRedPassed { (events, results, lines) =>

        val props = events.zip(lines).map {
          case (event, line) => line.contains(s"${Colours.green}${event.testName}") :|
            s"Line: [${line}] does not have testName: ${event.testName} following green colour code"
        }

        Prop.all(props:_*)
      }

  property("passed test line should be the sum of all its parts") =
      littleRedPassed { (events, results, lines) =>
        val props = events.zip(lines).map {
          case (event, line) =>
            val lineStructure = padding + Colours.green + event.testName + Colours.reset
            (lineStructure.length ?= line.length) :|
              s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
        }

        Prop.all(props:_*)
      }

  private def littleRedPassed(propertyAssertions: (Seq[RecordedEvent], Seq[Output], Seq[String]) => Prop): Prop = {
    Prop.forAll(arbitrary[String], genListOfRecordedPassedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case Line(line) => line
        }

        propertyAssertions(events, results, lines)
      }
  }
}