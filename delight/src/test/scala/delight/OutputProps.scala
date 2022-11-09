package delight

import org.scalacheck.Properties
import org.scalacheck._
import org.scalacheck.Prop._
import Gens._

object OutputProps extends Properties("Output.shows") {
  property("removing NoOutput has no effect") =
    Prop.forAll(genListOfOutput) { (outputs: List[Output]) =>
        Output.shows(outputs.filterNot(_ == NoOutput)) == Output.shows(outputs)
    }

  property("all non-empty Lines should be in the output") = {
    Prop.forAll(genListOfOutput) {  (outputs: List[Output]) =>
      val lines = outputs.collect {
        case Line(value) if value.nonEmpty => value
      }

      Output.shows(outputs).filter(out => out.nonEmpty && lines.contains(out)) =? lines
    }
  }

  property("MultiLines map to multiple shows lines") = {
    Prop.forAll(genListOfOutput) {  (outputs: List[Output]) =>
      val hasMultiLines = outputs.exists {
        case _: MultiLine => true
        case _ => false
      }

      if (hasMultiLines) {
        val outputWithoutNoOutputs = outputs.filterNot(_ == NoOutput)
        val showsLength = Output.shows(outputWithoutNoOutputs).length
        (outputWithoutNoOutputs.length < showsLength) :|
          s"outputs length:${outputs.length} should be less than shows length:${showsLength} when MultiLines exist"
      } else Prop(true)
    }
  }

  property("can replace MultiLines with their embedded Lines") = {
    Prop.forAll(genListOfOutput) {  (outputs: List[Output]) =>
      val multiLinesAsLines: Seq[Output] = outputs.collect {
        case MultiLine(value1, value2, rest@_*) => (value1 +: value2 +: rest).collect { case l@Line(_) => l }
      }.flatten

      val onlyMultiLines = outputs.collect { case ml: MultiLine => ml }

      Output.shows(multiLinesAsLines) =? Output.shows(onlyMultiLines)
    }
  }
}
