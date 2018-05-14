package delight

import org.scalatest.events._

final case class RecordedEvent(
  ordinal: Ordinal,
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
case object NoOutput extends Output