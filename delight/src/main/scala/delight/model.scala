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

final case class ClassName(value: String)
final case class MethodName(value: String)

final case class ClassElement(className: ClassName, methodName: MethodName)