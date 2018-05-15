package delight

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen._

object Gens {

    def genRecordedEvent: Gen[RecordedEvent] = for {
      runId          <- posNum[Int]
      suiteName      <- arbitrary[String]
      suiteId        <- arbitrary[String]
      suiteClassName <- arbitrary[String]
      testName       <- arbitrary[String]
      testText       <- arbitrary[String]
      timeStamp      <- posNum[Long]
      status         <- oneOf(Passed, Failed)
    } yield RecordedEvent(
              RunId(runId),
              suiteName,
              suiteId,
              suiteClassName,
              testName,
              testText,
              None,
              None,
              timeStamp,
              status,
              None
            )

  def genListOfRecordedPassedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedEvent.map(re => re.copy(status = Passed, throwable = None)))
  }

  def genListOfRecordedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedEvent)
  }
}