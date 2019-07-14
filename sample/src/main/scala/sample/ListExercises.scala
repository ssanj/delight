package sample

//replace implementations or use ??? to rest out delight
//testOnly *SomeTest -- -C delight.LittleRed or
//testOnly *SomeTest -- -C delight.Nature
object ListExercises {

  def prepend[A](value: A, list: List[A]): List[A] = value :: list

  def append[A](list: List[A], value: A): List[A] = list :+ value

  def isEmpty[A](list: List[A]): Boolean =
    list match {
      case Nil => true
      case _   => false
    }
}