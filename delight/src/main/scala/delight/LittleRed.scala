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
            val stOp = showStackTrace(error).headOption
            val st   = stOp.fold("")(firstSt => s"${strackTracePadding}${firstSt}")

            MultiLine(Line(s"${messagePadding}${red}${v.testName}${reset}"),
                      Line(s"${errorMessage}${st}"))


          case (_, _) => Line(s"${messagePadding}${red}${v.testName}${reset}")
        }
    }.toList

    heading +: (passedLines ++ failedLines)
  }

  private[LittleRed] val messagePadding      = "  - "
  private[LittleRed] val errorMessagePadding = "    > "
  private[LittleRed] val strackTracePadding  = " | "

}