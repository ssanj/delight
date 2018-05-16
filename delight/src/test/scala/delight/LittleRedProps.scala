package delight

import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._

object LittleRedProps extends Properties("LittleRed") {

  private val padding             = "  - "
  private val errorMessagePadding = "    > "
  private val stackTracePadding   = " | "

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
    littleRedPassed { (events, lines) =>
      def allLinesContainTestNameProp = {
        val props = lines.zip(events).map {
          case (line, event) => line.contains(event.testName) :| s"Line:$line doesn't contain testName:${event.testName}"
        }

        Prop.all(props:_*)
      }

      allLinesContainTestNameProp
    }

  property("passed test should start with padding") =
      littleRedPassed { (_, lines) =>
        val props = lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")
        Prop.all(props:_*)
      }

  property("passed test should end with console colour reset") =
      littleRedPassed { (_, lines) =>
        val props = lines.map(l => l.endsWith(Colours.reset) :| s"Line: [${l}] does not end with colour reset")
        Prop.all(props:_*)
      }

  property("passed test have green colour code") =
      littleRedPassed { (_, lines) =>
        val props = lines.map(l => l.contains(Colours.green) :| s"Line: [${l}] does not contain colour green")
        Prop.all(props:_*)
      }

  property("passed test name should follow green colour code") =
      littleRedPassed { (events, lines) =>

        val props = events.zip(lines).map {
          case (event, line) => line.contains(s"${Colours.green}${event.testName}") :|
            s"Line: [${line}] does not have testName: ${event.testName} following green colour code"
        }

        Prop.all(props:_*)
      }

  property("passed test line should be the sum of all its parts") =
      littleRedPassed { (events, lines) =>
        val props = events.zip(lines).map {
          case (event, line) =>
            val lineStructure = padding + Colours.green + event.testName + Colours.reset
            (lineStructure.length ?= line.length) :|
              s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
        }

        Prop.all(props:_*)
      }

  property("failed test should start with padding") =
      littleRedFailed { (_, lines) =>
        val props = lines.map(line => line.startsWith(padding) :| s"Line: [${line}] does not start with [${padding}]")
        Prop.all(props:_*)
      }

  property("failed test should end with console colour reset") =
      littleRedFailed { (_, lines) =>
        val props = lines.map(line => line.endsWith(Colours.reset) :| s"Line: [${line}] does not end with colour reset")
        Prop.all(props:_*)
      }

  property("failed test should have the colour red") =
      littleRedFailed { (_, lines) =>
        val props = lines.map(line => line.contains(Colours.red) :| s"Line: [${line}] does not end with colour red")
        Prop.all(props:_*)
      }

  property("failed test name should follow colour red") =
      littleRedFailed { (events,lines) =>
        val props = events.zip(lines).map {
          case (event, line) =>
            line.contains(s"${Colours.red}${event.testName}") :|
            s"Line: [${line}] does not have testName: ${event.testName} following red colour code"
        }

        Prop.all(props:_*)
      }

  property("failed test line should be the sum of all its parts") =
      littleRedFailed { (events, lines) =>
        val props = events.zip(lines).map {
          case (event, line) =>
            val lineStructure = padding + Colours.red + event.testName + Colours.reset
            (lineStructure.length ?= line.length) :|
              s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
        }

        Prop.all(props:_*)
      }

  property("should only display one failed test") =
    littleRedFailed { (events, lines) =>
      //even if there are more than one failed test, there should be only one displayed
      if (events.length >= 1) (lines.length == 1) :| "There should be only one failed test"
      else true
    }

  //TODO: Move this out
  property("stacktrace") =
    littleRedFailedWithStackTrace { (event, stacktrace) =>

      val lengthProp = (stacktrace.length == 1) :| s"should only have a single stacktrace line. Got: ${stacktrace.mkString}"
      lengthProp && {
        val st = stacktrace(0)
        val errorPaddingProp = st.startsWith(errorMessagePadding) :| s"stacktrace should start with [${errorMessagePadding}]: ${st}"

        val exception = event.throwable.get
        val containsErrorMessageProp = st.contains(exception.getMessage) :| s"stacktrace should contain Exception message: ${exception.getMessage} but got: ${st}"

        val stPaddingProp = st.contains(stackTracePadding) :| s"stacktrace should contain stackTracePadding before stacktrace: ${st}"

        val stContentProp = {
          val ste = exception.getStackTrace()(0)
          st.contains(ste.getFileName) :| s"stacktrace: ${st} should contain fileName: ${ste.getFileName}" &&
          st.contains(ste.getLineNumber.toString) :| s"stacktrace: ${st} should contain line number: ${ste.getLineNumber}" &&
          st.contains(":") :| s"stacktrace: ${st} should contain a colon" &&
          st.contains("(") :| s"stacktrace: ${st} should contain a (" &&
          st.contains(")") :| s"stacktrace: ${st} should contain a )"
        }

        errorPaddingProp &&
        containsErrorMessageProp &&
        stPaddingProp &&
        stContentProp
      }
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

  private def littleRedFailedWithStackTrace(propertyAssertions: (RecordedEvent, Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedFailedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        //only collect lines with a message and a stacktrace
        val stacktraces = results.drop(1).collect {
          case MultiLine(_, Line(stacktrace), _*) => stacktrace
        }

        if (events.nonEmpty) propertyAssertions(events.head, stacktraces)
        else Prop(true)
      }
}