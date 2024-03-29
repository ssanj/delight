package sample

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import ListExercises._

final class ListExercisesSpec extends AnyWordSpec with Matchers {
  "A List" should {
    "prepend a value" when {
      "it is empty" in {
        prepend(1, List.empty[Int]) should be (List(1))
      }

      "it is not empty" in {
        prepend(5, List(6,7,8,9)) should be (List(5, 6,7,8,9))
      }
    }

    "append a value" when {
      "it is empty" in {
        append(List.empty[Int], 1) should be (List(1))
      }

      "it is not empty" in {
        append(List(6,7,8,9), 10) should be (List(6,7,8,9,10))
      }
    }

    "be empty" when {
      "it has no elements" in {
        isEmpty(List.empty[Int]) should be (true)
        isEmpty(List.empty[String]) should be (true)
        isEmpty(List.empty[Boolean]) should be (true)
      }
    }

    "not be empty" when {
      "it has elements" in {
        isEmpty(List(1)) should be (false)
        isEmpty(List("this", "is", "test")) should be (false)
        isEmpty(List(true, false)) should be (false)
      }
    }
  }
}
