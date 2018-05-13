package delight

import Colours._
import StackTraceFunctions.showStackTrace

final class LittleRed extends CollectedEventsReporter {

  override def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Unit = {

    println(s"${green}${suiteClassName}:${reset}")

    val (failed ,passed) = events.partition(_.status == Failed)

    passed.foreach { v =>
      println(s"  - ${green}${v.testName}${reset}")
    }

    failed.take(1).foreach { v =>
      println(s"  - ${red}${v.testName}${reset}")
        (v.status, v.throwable) match {
          case (Failed, Some(error)) =>
            val errorMessage = s"${messagePadding}${error.getMessage}"
            val stacktraces = showStackTrace(error).take(1)
            val testLine = stacktraces match {
              case Seq() => ""
              case first => s"${strackTracePadding}${first.mkString}"
            }

            println(errorMessage + testLine)

          case (_, _) =>
        }
    }
  }

  private[LittleRed] val messagePadding = "    > "
  private[LittleRed] val strackTracePadding = " | "

}