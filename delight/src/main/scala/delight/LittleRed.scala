package delight

import Colours._
import StackTraceFunctions.showStackTrace

final class LittleRed extends CollectedEventsReporter {

  override def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Seq[Output] = {

    val heading = Line(s"${green}${suiteClassName}:${reset}")

    val (failed ,passed) = events.partition(_.status == Failed)

    val passedLines = passed.map { v =>
      Line(s"${messagePadding}${green}${v.testName}${reset}")
    }

    val failedLines = failed.headOption.map { v =>
        (v.status, v.throwable) match {
          case (Failed, Some(error)) =>
            val errorMessage = s"${errorMessagePadding}${error.getMessage}"
            val stacktraces = showStackTrace(error).take(1)
            val testLine = stacktraces match {
              case Seq() => ""
              case first => s"${strackTracePadding}${first.mkString}"
            }

            MultiLine(Line(s"${messagePadding}${red}${v.testName}${reset}"),
                      Line(errorMessage + testLine))


          case (_, _) => Line(s"${messagePadding}${red}${v.testName}${reset}")
        }
    }

    heading +: (passedLines ++ failedLines)
  }

  private[LittleRed] val messagePadding      = "  - "
  private[LittleRed] val errorMessagePadding = "    > "
  private[LittleRed] val strackTracePadding  = " | "

}