package delight

import Colours._
import StackTraceFunctions._

final class Nature extends CollectedEventsReporter {

  override def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Seq[Output] = {
    val (passed, failed) = events.foldLeft((0, 0)) {
      case (acc, RecordedEvent(_,_,_,_,_,_,_,_,_,Passed,_)) => (acc._1 + 1, acc._2)
      case (acc, RecordedEvent(_,_,_,_,_,_,_,_,_,Failed,_)) => (acc._1, acc._2 + 1)
    }

    val headingLine =
      Line(s"${green}${suiteClassName}:${reset} passed:${green}${passed}${reset} failed:${red}${failed}${reset} total:${passed + failed}")

    val eventLines =
      events.map { v =>

          val status = v.status match {
            case Passed => s"  - ${cyan}${v.testName}${reset} [${green}PASSED${reset}]"
            case Failed => s"  - ${cyan}${v.testName}${reset} [${red}FAILED${reset}]"
          }

          val statusLine = Line(status)

          (v.status, v.throwable) match {
            case (Failed, Some(error)) =>
              val errorMessage = s"${errorMessagePadding}${error.getMessage}"
              val stackTraceOp = showStackTrace(error).headOption
              stackTraceOp.fold(MultiLine(statusLine, Line(errorMessage))) { stackTrace =>
                MultiLine(
                  statusLine,
                  Line(s"${errorMessage}${strackTracePadding}${stackTrace}")
                )
              }

              case (_, _) => statusLine
          }
      }

    headingLine +: eventLines
  }

  private[Nature] val errorMessagePadding = "    > "
  private[Nature] val strackTracePadding  = " | "

}