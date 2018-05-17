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

  object PassedTest {

    def startWithPadding(lines: Seq[String]): Prop = {
      val props = lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")
      Prop.all(props:_*)
    }

    def endWithColourReset(lines: Seq[String]): Prop = {
      val props = lines.map(l => l.endsWith(Colours.reset) :| s"Line: [${l}] does not end with colour reset")
      Prop.all(props:_*)
    }

    // def haveGreenColour(lines: Seq[String]): Prop = {
    //   val props = lines.map(l => l.contains(Colours.green) :| s"Line: [${l}] does not contain colour green")
    //   Prop.all(props:_*)
    // }

    def nameShouldBeGreenColour(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
      val props = events.zip(lines).map {
        case (event, line) => line.contains(s"${Colours.green}${event.testName}") :|
          s"Line: [${line}] does not have testName: ${event.testName} following green colour code"
      }

      Prop.all(props:_*)
    }

    def outputShouldHaveExpectedLength(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
      val props = events.zip(lines).map {
        case (event, line) =>
          val lineStructure = padding + Colours.green + event.testName + Colours.reset
          (lineStructure.length ?= line.length) :|
            s"Line: [${line}] of length: ${line.length} is not equal to Structure: [${lineStructure}] of length: ${lineStructure.length}"
      }

      Prop.all(props:_*)
    }

    def allLinesContainTestNameProp(events: Seq[RecordedEvent], lines: Seq[String]): Prop = {
      val props = lines.zip(events).map {
        case (line, event) => line.contains(event.testName) :| s"Line:$line doesn't contain testName:${event.testName}"
      }

      Prop.all(props:_*)
    }

    def properties: Prop =
      littleRedPassed { (events, lines) =>
        startWithPadding(lines) &&
        allLinesContainTestNameProp(events, lines) &&
        nameShouldBeGreenColour(events, lines) &&
        endWithColourReset(lines) &&
        outputShouldHaveExpectedLength(events, lines)
      }
  }

  property("passed test properties") = PassedTest.properties


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

  property("failed tests should only have  single stacktrace line") =
    littleRedFailedWithStackTrace { (event, stacktrace) =>
      (stacktrace.length == 1) :| s"expected single stacktrace line. Got: ${stacktrace.mkString(",")}"
    }

  property("failed tests should have stacktrace info") =
    littleRedFailedWithStackTrace { (event, stacktrace) =>
        val st = stacktrace(0)

        val withouterrorMessagePadding = st.stripPrefix(errorMessagePadding)
        val errorMessagePaddingProp =
          (withouterrorMessagePadding.length == (st.length - errorMessagePadding.length)) :|
            s"stacktrace: [${st}] should contain errorMessagePadding: [${errorMessagePadding}]"

        val exception = event.throwable.get
        val exceptionMessage = exception.getMessage

        val withoutExceptionMessage = withouterrorMessagePadding.stripPrefix(exceptionMessage)
        val exceptionMessageProp =
          (withoutExceptionMessage.length == (withouterrorMessagePadding.length - exceptionMessage.length)) :|
            s"stacktrace: [${st}] should contain exceptionMessage: [${exceptionMessage}]"

        val withoutstackTracePadding = withoutExceptionMessage.stripPrefix(stackTracePadding)
        val stackTracePaddingProp =
          (withoutstackTracePadding.length == (withoutExceptionMessage.length - stackTracePadding.length)) :|
            s"stacktrace: [${st}] should contain stackTracePadding: [${stackTracePadding}]"

        val openBraceProp = (withoutstackTracePadding.startsWith("(")) :| s"stacktrace: [${st}] should contain ("
        val closeBraceProp = (withoutstackTracePadding.endsWith(")"))  :| s"stacktrace: [${st}] should contain )"

        val fileAndLine = withoutstackTracePadding.drop(1).dropRight(1)
        val ste = exception.getStackTrace()(0)
        val parts = fileAndLine.split(":")

        val fileNameProp = (parts(0) == ste.getFileName) :|
          s"stacktrace: [${st}] should contain fileName: ${ste.getFileName}"
        val lineNumberProp = (parts(1) == ste.getLineNumber.toString) :|
          s"stacktrace: [${st}] should contain fileName: ${ste.getLineNumber}"

        errorMessagePaddingProp &&
        exceptionMessageProp &&
        stackTracePaddingProp &&
        openBraceProp &&
        fileNameProp &&
        lineNumberProp &&
        closeBraceProp
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