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

  def genThrowable: Gen[Throwable] = for {
    message <- arbitrary[String]
    error   <- oneOf(new IllegalArgumentException(message),
                     new IllegalStateException(message),
                     new java.sql.SQLException(message),
                     new java.util.concurrent.TimeoutException(message),
                     new Exception(message),
                     new RuntimeException(message))
  } yield error

  def genListOfRecordedPassedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedEvent.map(re => re.copy(status = Passed, throwable = None)))
  }

  def genListOfRecordedFailedEvent: Gen[List[RecordedEvent]] = {
    val genWithError =
      for {
        re    <- genRecordedEvent
        error <- genThrowable
      } yield re.copy(status = Failed, throwable = Some(error))

    sized(listOfN(_, genWithError))
  }

  def genListOfRecordedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedEvent)
  }
}