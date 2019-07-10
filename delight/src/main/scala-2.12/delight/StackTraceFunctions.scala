package delight

object StackTraceFunctions {

  def showFilteredStackTrace(pred: StackTraceElement => Boolean, show: StackTraceElement => String)(throwable: Throwable): Seq[String] = {
    getStackTrace(throwable).filterNot(pred).map(show)
  }

  def showStackTrace(throwable: Throwable): Seq[String] = {
    showFilteredStackTrace(filterOut(ignored), showStackTraceElement)(throwable)
  }

  def getStackTrace(throwable: Throwable): Seq[StackTraceElement] = {
    import scala.collection.JavaConverters._
    import java.util.Arrays
    Arrays.asList(throwable.getStackTrace:_*).asScala
  }

  def filterOut(ignored: Seq[String])(stacktrace: StackTraceElement): Boolean = {
    ignored.exists(ig => stacktrace.getClassName.contains(ig))
  }

  val ignored: Seq[String] = Seq("scala.", "org.scalatest", "sbt.", "java.")

  def showStackTraceElement(ste: StackTraceElement): String = {
    s"(${ste.getFileName}:${ste.getLineNumber.toString})"
  }
}