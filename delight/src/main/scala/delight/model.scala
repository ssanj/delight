package delight

import org.scalatest.events._

final case class RunId(value: Int)

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
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

sealed trait Output extends Product with Serializable
final case class Line(value: String) extends Output
final case class MultiLine(value1: Output, value2: Output, values: Output*) extends Output
case object NoOutput extends Output

//TODO: Test
object Output {
  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def shows(outputs: Seq[Output]): Seq[String] = outputs match {
    case Seq(Line(value), rest@_*)                          => value +: shows(rest)
    case Seq(MultiLine(Line(v1), v2, more@_*), rest@_*)     => v1 +: shows(v2 +: (more ++ rest))
    case Seq(MultiLine(m: MultiLine, v2, more@_*), rest@_*) => shows(m +: v2 +: (more ++ rest))
    case Seq(MultiLine(NoOutput, v2, more@_*), rest@_*)     => shows(v2 +: (more ++ rest))
    case Seq(NoOutput, rest@_*)                             => shows(rest)
    case Seq()                                              => Seq.empty[String]
  }
}