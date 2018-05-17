package delight
package littlered

import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._

object WithStackTrace {

  private val errorMessagePadding = "    > "
  private val stackTracePadding   = " | "

  def properties: Prop =
    littleRedFailedWithStackTrace { (event, stacktrace) =>
      val haveSingleStackTraceLine: Prop =
        (stacktrace.length == 1) :| s"expected single stacktrace line. Got: ${stacktrace.mkString(",")}"

      def stackTraceInfo(st: String): Prop = {
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

      haveSingleStackTraceLine &&
      stacktrace.foldLeft(false :| "expected one stacktrace line")((_, v) => stackTraceInfo(v))
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