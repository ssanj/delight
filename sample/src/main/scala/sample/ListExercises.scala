package sample

object ListExercises {

  def append[A](value: A, list: List[A]): List[A] = value :: list

  def prepend[A](list: List[A], value: A): List[A] = list :+ value

  def isEmpty[A](list: List[A]): Boolean = list.foldLeft(true)((_, _) => false)
}