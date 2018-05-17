package delight
package littlered

import org.scalatest.{Matchers, WordSpecLike}
import org.scalacheck.Arbitrary.arbitrary
import Gens._

final class LittleRedSpec extends Matchers with WordSpecLike {

  "LittleRed" should {
    "not write out stacktrace" when {
      "one does not exist" in {
        val suiteName = arbitrary[String].sample.get
        val events = Seq(genFailedRecordedEventWithoutStackTrace.sample.get)

        val reporter = new LittleRed
        val results  = reporter.processEvents(suiteName, events)

        val stacktraces = results.drop(1).collect {
          case MultiLine(_, Line(stacktrace), _*) => stacktrace
        }

        events.
          headOption.
          flatMap(_.throwable).
          map(_.getMessage).
          fold(
            fail("expected one stacktrace element")
          ) { error =>
            stacktraces should have size (1)
            stacktraces(0).stripPrefix("    > ") should be (error)
          }
      }
    }
  }
}