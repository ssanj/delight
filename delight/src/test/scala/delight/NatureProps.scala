package delight

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary.arbitrary
import Gens._
import PropUtil._

object NatureProps extends Properties("Nature") {

  property("should have a formatted heading") = headingProp

  def headingProp: Prop = natureEvents { (suiteName, events, outputs) =>

    def notEmpty(lines: Seq[Output]): Prop = lines.length > 0

    def headerFormat(heading: Output): Prop = {
      heading match {
        case Line(line) =>
          val sections = line.split(" ")
          val passed   = events.filter(_.status == Passed).length
          val failed   = events.length - passed
          val total    = events.length
          Prop.all(
            (sections.length ?= 4) :| s"Expected 4 sections but got: ${sections.length}, for line: [${line}]",
            coloured("", Colours.green, s"${suiteName}:")(sections(0)) :| s"prefix section for line: [${line}]",
            coloured("passed:", Colours.green, passed.toString)(sections(1)) :| s"passed section for line: [${line}]",
            coloured("failed:", Colours.red, failed.toString)(sections(2)) :| s"failed section for line: [${line}]",
            (sections(3) ?= s"total:${total}") :| s"total section for line: [${line}]"
          )
        case other => false :| s"Expected Line but got: ${other}"
      }
    }

    notEmpty(outputs) &&
    headerFormat(outputs.head)
  }

  private def natureEvents(propertyAssertions: (String, Seq[RecordedEvent], Seq[Output]) => Prop): Prop =
    Prop.forAll(arbitrary[String], genListOfRecordedEvent) {
      case (suiteName, events) =>
        val reporter = new Nature
        val results  = reporter.processEvents(suiteName, events)

        propertyAssertions(suiteName, events, results)
      }


}