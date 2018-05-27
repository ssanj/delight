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

sealed trait LineType { val value: String }

sealed trait Output extends Product with Serializable
final case class Line(value: String) extends Output with LineType
final case class MultiLine(value1: LineType, value2: LineType, values: LineType*) extends Output
case object NoOutput extends Output

//TODO: Test
object Output {
  def shows(outputs: Seq[Output]): Seq[String] = outputs.foldRight(Seq.empty[String]){
    case (Line(value), acc)              => value +: acc
    case (MultiLine(v1, v2, vx@_*), acc) => (v1 +: v2 +: vx).map(_.value) ++ acc
    case (NoOutput, acc)                 => acc
  }
}