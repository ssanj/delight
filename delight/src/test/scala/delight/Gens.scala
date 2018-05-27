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
      throwable      <- genThrowable
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
              if (status == Failed) Some(throwable) else None
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

  final case class ClassName(value: String)
  final case class MethodName(value: String)
  final case class FileName(value: String)
  final case class LineNumber(value: Int)

  def genClassName: Gen[ClassName] = for {
    length    <- choose(3, 6)
    prefix    <- listOfN(length, alphaLowerStr).map(_.mkString("."))
    upperChar <- alphaUpperChar
    lowers    <- listOfN(length, alphaLowerChar).map(_.mkString)
  } yield ClassName(s"${prefix}.${upperChar}${lowers}")

  def genMethodName: Gen[MethodName] = for {
    length    <- choose(3, 6)
    prefix    <- listOfN(length, alphaLowerChar)
    upperChar <- alphaUpperChar
    lowers    <- listOfN(length, alphaLowerChar)
  } yield MethodName(s"${prefix}${upperChar}${lowers}")

  def genFileName: Gen[FileName] = for {
    length   <- choose(4, 6)
    filename <- listOfN(length, alphaLowerChar).map(_.mkString)
  } yield FileName(s"${filename}.scala")

  def genLineNumber: Gen[LineNumber] = posNum[Int].map(LineNumber)

  def genStackTraceElement: Gen[StackTraceElement] = for {
    className  <- genClassName.map(_.value)
    methodName <- genMethodName.map(_.value)
    fileName   <- genFileName.map(_.value)
    line       <- genLineNumber.map(_.value)
  } yield new StackTraceElement(className, methodName, fileName, line)

  def genListOfStackTraceElement: Gen[List[StackTraceElement]] = for {
    length   <- choose(2, 3)
    traces   <- listOfN(length, genStackTraceElement)
  } yield traces

  def genFailedRecordedEventWithStackTrace: Gen[RecordedEvent] = for {
      re    <- genRecordedEvent
      st    <- genListOfStackTraceElement
      error <- genThrowable.map{ e => e.setStackTrace(st.toArray); e}
    } yield re.copy(status = Failed, throwable = Some(error))

  def genFailedRecordedEventWithoutStackTrace: Gen[RecordedEvent] = for {
      re      <- genRecordedEvent
      message <- arbitrary[String]
      error   <- new scala.util.control.NoStackTrace { override def getMessage = message }
    } yield re.copy(status = Failed, throwable = Some(error))


  def genListOfRecordedFailedEvent: Gen[List[RecordedEvent]] = {
    sized(listOfN(_, genFailedRecordedEventWithStackTrace))
  }

  def genListOfRecordedFailedWithoutStackTraceEvent: Gen[List[RecordedEvent]] = {
    sized(listOfN(_, genFailedRecordedEventWithoutStackTrace))
  }

  def genListOfRecordedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedEvent)
  }
}