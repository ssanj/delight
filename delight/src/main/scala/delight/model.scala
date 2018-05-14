package delight

import org.scalatest.events._

final case class RunId(value: Int)

final case class RecordedEvent(
  runId: RunId,
  suiteName: String,
  suiteId: String,
  suiteClassName: String,
  testName: String,
  testText: String,
  location: Option[Location],
  payload: Option[Any] = None,
  timeStamp: Long,
  status: TestStatus,
  throwable: Option[Throwable]
)

//ADT for Test Statuses
sealed trait TestStatus
final case object Passed extends TestStatus
final case object Failed extends TestStatus

sealed trait Output
final case class Line(value: String) extends Output
final case class MultiLine(value1: Line, value2: Line, values: Line*) extends Output
case object NoOutput extends Output