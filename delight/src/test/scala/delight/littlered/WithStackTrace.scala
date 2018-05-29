package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._
import StackTraceFunctions.ignored
import StackTraceFunctions.showStackTraceElement

object WithStackTrace {

  private val errorMessagePadding = "    > "
  private val stackTracePadding   = " | "

  def properties: Prop =
    littleRedFailedWithStackTrace { (event, stacktrace) =>

      def hasSingleStackTraceLine(st: Seq[String]): Prop =
        (st.length == 1) :| s"expected single stacktrace line. Got: ${st.mkString(",")}"

      def stackTraceInfo(line: String): Prop = {
        val exception = event.throwable.get
        val exceptionMessage = exception.getMessage
        val st = exception.
                  getStackTrace.
                  filterNot(ste => ignored.exists(ste.getClassName.contains)).
                  headOption.
                  map(showStackTraceElement).
                  get

        errorMessagePadding :> exceptionMessage :> stackTracePadding :> st | line
      }

      hasSingleStackTraceLine(stacktrace) &&
      stackTraceInfo(stacktrace(0))
    }

  private def littleRedFailedWithStackTrace(propertyAssertions: (RecordedEvent, Seq[String]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedFailedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val stacktraces = results.drop(1).collect {
          case MultiLine(_, Line(stacktrace), _*) => stacktrace
        }

        if (events.nonEmpty) propertyAssertions(events.head, stacktraces)
        else Prop(true)
      }
}