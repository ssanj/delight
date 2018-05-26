package delight

import org.scalacheck.Gen
import Gens._

//There functions should ideally be on the TestStatus ADT.
//Unfortunately these functions deal with test-specific behaviour
//and as such shouldn't be against the production model.
//We create ColourStatus here to namespace these functions.
object ColourStatus {

  def colour: TestStatus => String = {
    case Passed  => Colours.green
    case Failed  => Colours.red
  }

  def name: TestStatus => String = {
    case Passed  => "PASSED"
    case Failed  => "FAILED"
  }

  def gen: TestStatus => Gen[List[RecordedEvent]] = {
    case Passed  => genListOfRecordedPassedEvent
    case Failed  => genListOfRecordedFailedEvent
  }
}