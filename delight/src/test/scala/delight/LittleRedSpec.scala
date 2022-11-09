package delight
package littlered

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalacheck.Arbitrary.arbitrary
import Gens._

final class LittleRedSpec extends AnyWordSpec with Matchers {

  "LittleRed" should {
    "not write out stacktrace" when {
      "one does not exist" in {
        val suiteName = arbitrary[String].sample.get
        val events = Seq(genFailedRecordedEventWithoutStackTrace.sample.get)

        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val errorLine = results.drop(1).collect {
          case MultiLine(_, Line(error), _*) => error
        }

        events.
          headOption.
          flatMap(_.throwable).
          map(_.getMessage).
          fold(
            fail("expected error message")
          ) { error =>
            errorLine should have size (1)
            errorLine(0).stripPrefix("    > ") should be (error)
          }
      }
    }
  }
}
