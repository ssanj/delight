package delight

import org.scalatest.Reporter
import org.scalatest.events._
import scala.collection.mutable.ListBuffer

trait CollectedEventsReporter extends Reporter {

  private val events = new ListBuffer[RecordedEvent]

  override def apply(event: Event): Unit = {
    event match {
      case TestFailed(ordinal, message, suiteName, suiteId, Some(suiteClassName), testName, testText, _,
                        throwable, _, _, location, _, payload, _, timestamp) =>
          events += RecordedEvent(
                      RunId(ordinal.runStamp),
                      suiteName,
                      suiteId,
                      suiteClassName,
                      testName,
                      testText,
                      location,
                      payload,
                      timestamp,
                      Failed,
                      throwable
                    )

      case TestSucceeded (ordinal, suiteName, suiteId, Some(suiteClassName), testName, testText, _,
                            _, _, location, _, payload, _, timestamp) =>
        events += RecordedEvent(
                    RunId(ordinal.runStamp),
                    suiteName,
                    suiteId,
                    suiteClassName,
                    testName,
                    testText,
                    location,
                    payload,
                    timestamp,
                    Passed,
                    None
                  )

      case r: RunCompleted =>
        events.groupBy(_.suiteClassName).map {
          case (k, values) =>
            println(
              processEvents(k, values).collect {
                case Line(line) => line
                case MultiLine(line1, line2, other@_*) => (line1 +: line2 +: other).map(_.value).mkString("\n")
              }.mkString("\n")
            )
        }

      case _ =>
    }
  }

  def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Seq[Output]

}