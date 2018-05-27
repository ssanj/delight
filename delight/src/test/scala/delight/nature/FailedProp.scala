package delight
package nature

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import PropUtil._
import Gens._
import StackTraceFunctions._

object FailedProp {

  final case class Failure(status: StatusLine, error: ErrorLine)
  final case class StatusLine(line: String)
  final case class ErrorLine(line: String)

  private val padding = "  - "
  private val errorMessagePadding = "    > "
  private val strackTracePadding  = " | "

  def properties: Prop =
    natureFailed{ (events, lines) =>
      startWithPadding(lines.map(_.status.line))(padding) &&
      failedLineFormatting(events, lines.map(_.status)) &&
      stackTrace(events, lines.map(_.error))
    }

  private def failedLineFormatting(events: Seq[RecordedEvent], lines: Seq[StatusLine]): Prop =
    events.zip(lines).map {
      case (event, StatusLine(line)) =>
        val split = line.stripPrefix(padding).split(" ")
        //if the test name has spaces then join all of them
        val sections  = if (split.length > 2) Array(split.init.mkString(" "), split.last) else split
        Prop.all(
          (sections.length ?= 2) :| s"Expected 2 sections but got: ${sections.length}, ${sections.mkString(",")}",
          coloured("", Colours.cyan, event.testName)(sections(0)),
          sections(1).startsWith("[") :| s"Section does not start with '[': ${sections(1)}",
          sections(1).endsWith("]") :| s"Section does not end with '[': ${sections(1)}",
          coloured("", Colours.red, "FAILED")(sections(1).drop(1).dropRight(1))
        )
    }

private def stackTrace(events: Seq[RecordedEvent], lines: Seq[ErrorLine]): Prop =
    events.zip(lines).map {
      case (event, ErrorLine(line)) =>
        val errorMessage = event.throwable.map(_.getMessage).getOrElse("-No-Error-Message-")
        val errorPaddingProp = line.startsWith(errorMessagePadding) :| s"Line: [${line}] does not start with error padding: [${errorMessagePadding}]"
        val errorMessageProp = (line.stripPrefix(errorMessagePadding).startsWith(errorMessage)) :| s"Line: [${line}] does not contain error message: [${errorMessage}]"
        val stackTraceSection = line.stripPrefix(errorMessagePadding + errorMessage)
        val stackTracePaddingProp = stackTraceSection.startsWith(strackTracePadding) :| s"Line: [${line}] does not contain stacktrace padding: [${strackTracePadding}]"
        val stackTraceElement =
            event.
              throwable.
              flatMap(t => getStackTrace(t).filterNot(t => ignored.exists(t.getClassName.contains)).map(showStackTraceElement).headOption)
              .getOrElse("-No-StackTrace-")

        val st = event.throwable.map(t => getStackTrace(t).map(st => st.getClassName + s"${showStackTraceElement(st)}")).toList.flatten

        val stackTraceProp = (stackTraceSection.stripPrefix(strackTracePadding) ?= stackTraceElement) :|
          s"Line: [${line}] does not contain stacktrace element: [${stackTraceElement}], full stacktrace: ${st.mkString("\n")}"

        errorPaddingProp &&
        errorMessageProp &&
        stackTracePaddingProp &&
        stackTraceProp
    }

  def natureFailed(propertyAssertions: (Seq[RecordedEvent], Seq[Failure]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedFailedEvent) {
      case (suiteName, events) =>
        val reporter = new Nature
        val results  = reporter.processEvents(suiteName, events)

        val lines = results.drop(1).collect {
          case MultiLine(Line(status), Line(error)) => Failure(StatusLine(status), ErrorLine(error))
        }

        propertyAssertions(events, lines)
      }
}