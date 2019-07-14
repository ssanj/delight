package delight

import org.scalatest.Reporter
import org.scalatest.events._
import scala.collection.mutable.ListBuffer

trait CollectedEventsReporter extends Reporter {

  private val events = new ListBuffer[RecordedEvent]

  @specialized def sideEffect[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () //Return unit to prevent warning due to discarding value
  }

  override def apply(event: Event): Unit = {
    event match {
      case TestFailed(ordinal, _, suiteName, suiteId, Some(suiteClassName), testName, testText, _,
                        throwable, _, _, location, _, payload, _, timestamp) =>
        sideEffect(
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
        )

      case TestSucceeded (ordinal, suiteName, suiteId, Some(suiteClassName), testName, testText, _,
                            _, _, location, _, payload, _, timestamp) =>
        sideEffect(
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
        )

      case _: RunCompleted =>
        sideEffect(
          events.groupBy(_.suiteClassName).map {
            case (k, values) => println(Output.shows(processEvents(k, values.toSeq)).mkString("\n"))
          }
        )

      case _ =>
    }
  }

  //TODO: Make this return Output
  def processEvents(suiteClassName: String, events: Seq[RecordedEvent]): Seq[Output]

}