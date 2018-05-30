package sample

object ListExercises {

  def prepend[A](value: A, list: List[A]): List[A] = value :: list

  def append[A](list: List[A], value: A): List[A] = list :+ value

  def isEmpty[A](list: List[A]): Boolean = list match {
    case Nil => true
    case _   => false
  }
}