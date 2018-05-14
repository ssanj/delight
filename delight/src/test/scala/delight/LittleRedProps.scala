package delight

import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary.arbitrary

object LittleRedProps extends Properties("LittleRed") {

  property("display suiteName in heading") =
    Prop.forAll(asciiStr) { suiteClassName: String =>
      val reporter = new LittleRed
      val results = reporter.processEvents(suiteClassName, Seq.empty[RecordedEvent])
      (results.length ?= 1) &&
      (results(0) match {
        case Line(line) => line.contains(suiteClassName) :| s"${results} did not contain suiteName: ${suiteClassName}"
        case other => false :| s"expected Line but got: $other"
      })
    }

  private def genRecordedPassedEvent: Gen[RecordedEvent] = for {
      runId          <- posNum[Int]
      suiteName      <- arbitrary[String]
      suiteId        <- arbitrary[String]
      suiteClassName <- arbitrary[String]
      testName       <- arbitrary[String]
      testText       <- arbitrary[String]
      timeStamp      <- posNum[Long]
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
              Passed,
              None
            )

  private def genListOfRecordedPassedEvent: Gen[List[RecordedEvent]] = sized {
    listOfN(_, genRecordedPassedEvent)
  }

  property("display passed tests") = {
    Prop.forAll(arbitrary[String], genListOfRecordedPassedEvent) {
      case (suiteName, events) =>
        val reporter = new LittleRed
        val results = reporter.processEvents(suiteName, events)

        def lengthProp = (results.length ?= (1 + events.length)) :| s"result length"

        val lines = results.drop(1).collect {
          case Line(line) => line
        }

        def linesOnlyProp =
          (lines.length ?= events.length) :| "should only contain Line instances"

        def allLinesContainTestNameProp = {
            val props = lines.zip(events).map {
              case (line, event) => line.contains(event.testName) :| s"Line:$line doesn't contain testName:${event.testName}"
            }

            Prop.all(props:_*)
        }

        lengthProp &&
        linesOnlyProp &&
        allLinesContainTestNameProp
    }
  }
}