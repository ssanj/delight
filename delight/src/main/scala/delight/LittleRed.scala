package delight

import Colours._
import StackTraceFunctions.showStackTrace

final class LittleRed extends CollectedEventsReporter {

  override def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Seq[Output] = {

    val heading = Line(s"${green}${suiteClassName}:${reset}")

    val (failed ,passed) = events.partition(_.status == Failed)

    val passedLines = passed.map { v =>
      Line(s"  - ${green}${v.testName}${reset}")
    }

    val failedLines = failed.headOption.map { v =>
      Line(s"  - ${red}${v.testName}${reset}")
        (v.status, v.throwable) match {
          case (Failed, Some(error)) =>
            val errorMessage = s"${messagePadding}${error.getMessage}"
            val stacktraces = showStackTrace(error).take(1)
            val testLine = stacktraces match {
              case Seq() => ""
              case first => s"${strackTracePadding}${first.mkString}"
            }

            Line(errorMessage + testLine)

          case (_, _) => NoOutput
        }
    }

    heading +: (passedLines ++ failedLines)
  }

  private[LittleRed] val messagePadding = "    > "
  private[LittleRed] val strackTracePadding = " | "

}