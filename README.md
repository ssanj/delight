# Delight #

[![Build Status](https://travis-ci.org/ssanj/delight.svg?branch=master)](https://travis-ci.org/ssanj/delight)

Delight is a small collection of [ScalaTest](http://www.scalatest.org/) Reporters that make running your tests a more pleasant experience. These reporters are meant to be run through [SBT](https://www.scala-sbt.org/) although there is nothing stopping you from running them through an IDE.

Often when participating in a workshop or even while refactoring some code, you might have find yourself drowning in a sea of broken tests:

![lots of failing tests](failing-tests.png)

Often the multiple Stacktrace lines shown are unnecessary and there is only a single useful Stacktrace line. Wouldn't it be nice if we didn't have to see all this superfluous information?

In a workshop you have to go through each module implementing solutions as you go and thereby fixing the tests. If there are lots of failing tests, it becomes hard to figure out which tests are failing and for what reason. Did you fix a test? It's almost impossible to see without scrolling to the top of your terminal.

In the case of a broken refactoring even though many tests fail, if you fix one test you usually end up fixing them all.

What if we could just reduce the noise of these failing tests and just focus on one thing at a time?


## Little Red

![Little Red](littlered.jpg)
[Litte Red from Threadless](https://www.threadless.com/product/1586/RED/style,design)


This reporter shows all tests that pass and only the first test that fails. Once you fix the first failing test, the next failing test is shown. The Stacktrace is also truncated to only one line with the most relevant information. The objective of this reporter is to get out of your way and help you focus on the task at hand.

We can use the LittleRed report supplying the following parameters to ScalaTest:

```
-- -C delight.LittleRed
```

For example to run the [ListExercisesSpec](https://github.com/ssanj/delight/blob/master/sample/src/test/scala/sample/ListExercisesSpec.scala) in the `sample` project, first switch to the sample project:

```
project sample
```

Next run Little Red:

```
~testOnly *ListExercisesSpec -- -C delight.LittleRed
```

With six failing tests, it only reports the first failing test:

![6 failing tests](fail1.png)

Let's fix the implementation of the `append` function:

```
def append[A](value: A, list: List[A]): List[A] = value :: list
```

Once we fix the implementation, we see the tests that were fixed plus the next failing test:

![4 failing tests](fail2.png)

Next let's fix the implementation of the `prepend` function:

```
def prepend[A](list: List[A], value: A): List[A] = list :+ value
```

After we fix the implementation we see the next failing tests:

![2 failing tests](fail3.png)

Once we fix the implementation of the `isEmpty` function:

```
def isEmpty[A](list: List[A]): Boolean = list.foldLeft(true)((_, _) => false)
```

we see that all the tests pass!

![all tests pass](success.png)