package delight

import scala.collection.GenTraversableOnce
import scala.language.implicitConversions
import org.scalacheck.Prop
import org.scalacheck.Prop._

object PropUtil {
  implicit def toProps(bools: GenTraversableOnce[Prop]): Prop = Prop.all(bools.toList:_*)

  def coloured(prefix: String, colour: String, text: String)(section: String): Prop = {
    Prop.all(
      section.startsWith(prefix) :| s"Section: [${section}] does not start with prefix: ${prefix}",
      section.stripPrefix(prefix).startsWith(colour)  :| s"Section: [${section}] does have ${colour}colour${Colours.reset} following prefix",
      section.stripSuffix(Colours.reset).endsWith(text) :| s"Section: [${section}] does not have text: [${text}] before reset",
      section.endsWith(Colours.reset) :| s"Section: [${section}] does not end with colour reset"
    )
  }

  def startWithPadding(lines: Seq[String])(padding: String): Prop =
    lines.map(l => l.startsWith(padding) :| s"Line: [${l}] does not start with [${padding}]")


  private[PropUtil] final case class Acc(line: String, prop: Prop)

  final case class Sequential(values: String*) {
    def on(line: String): Prop = {
      val lengthProp  = (line.length =? values.map(_.length).sum) :| s"Line: [${line}] has different length to values: [${values.mkString}]"
      val sectionProp = values.foldLeft(Acc(line, Prop(true))) { (acc, v) =>
        val prop = acc.line.startsWith(v) :| s"Line: [${line}], substring: [${acc.line}] doesn't start with [${v}]"
        Acc(acc.line.stripPrefix(v), acc.prop && prop)
      }.prop

      lengthProp && sectionProp
    }
  }

  implicit def toSymbolSequential(value: String): SymbolicSequential = SymbolicSequential(value)

  case class SymbolicSequential(values: String*) {
    def :> (valueNew: String): SymbolicSequential = SymbolicSequential((values :+ valueNew):_*)
    def |(line: String): Prop = Sequential(values:_*).on(line)
  }

  def sequentially(values: String*): Sequential = Sequential(values:_*)
}