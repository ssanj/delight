package sample

object ListExercises {

  def append[A](value: A, list: List[A]): List[A] = List(value)

  def prepend[A](list: List[A], value: A): List[A] = list

  def isEmpty[A](list: List[A]): Boolean = ???
}