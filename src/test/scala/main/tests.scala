package main

import org.scalatest._
import funcDep._
import FunctionalDependencies._

/**
  * Created by remi on 20/03/17.
  */
class tests extends FunSuite {

  val path = getClass.getResource("/ex.CC1.txt").getPath
  val path1 = getClass.getResource("/ex.TD3.txt").getPath
  val path2 = getClass.getResource("/generate100.txt").getPath
  val path3 = getClass.getResource("/generate5.txt").getPath
  val pathGenerate = List(
    "/generate5.txt",
    "/generate100.txt"
    ).map(getClass.getResource(_).getPath)
 
  test("naive algorithm works for simple cases") {
    val atts1 = Attributes("A")
    val atts2 = Attributes("A", "E", "C")
    val atts3 = Attributes.empty

    assert(closure1(atts1, path) == Attributes("A", "B", "D"))
    assert(closure1(atts2, path) == Attributes("A", "B", "D", "E", "C"))
    assert(closure1(atts3, path) == Attributes.empty)
  }

  test("improved algorithm works for simple cases") {
    val atts1 = Attributes.newFromStringSet(Set("A"))
    val atts2 = Attributes.newFromStringSet(Set("A", "E", "C"))
    val atts3 = Attributes.empty

    assert(closure2(atts1, path) == Attributes("A", "B", "D"))
    assert(closure2(atts2, path) == Attributes("A", "B", "D", "E", "C"))
    assert(closure2(atts3, path) == Attributes.empty)
  }

  test("both closure algorithm output the same result") {
    val atts1 = Attributes("A")
    val atts2 = Attributes("A", "E", "C")
    val atts3 = Attributes.empty

    assert(closure2(atts1, path) == closure1(atts1, path))
    assert(closure2(atts2, path) == closure1(atts2, path))
    assert(closure2(atts3, path) == closure1(atts3, path))

  }

  test("check function behaves correctly") {
    val atts = Attributes("A", "E", "C")

    val sigma = newFromFile(path)

    val X1 = Attributes("A")
    val Y1 = Attributes("B", "D")

    val X2 = Attributes("C", "E")
    val Y2 = Attributes("B")

    val X3 = Attributes.empty
    val Y3 = Attributes.empty

    assert(check(sigma, X1, Y1))
    assert(check(sigma, X2, Y2))
    assert(check(sigma, X3, Y3))
  }

  test("minimize") {
    pathGenerate.foreach { path =>
      val sigma = newFromFile(path)
      assert(sigma.minimize().getDependencies.forall {
        case (x, y) => y == algo2(sigma, x)
      })
    }
  }

  test("normalization") {
    pathGenerate.foreach { path =>
      val sigma = newFromFile(path)
      val n = sigma.normalize()
      println(n)
      assert(n.getDependencies.forall {
        case(x, y) => Integer.parseInt(x.head.getName) == Integer.parseInt(y.head.getName) - 1
      })
    }
  }

  test("decompose") {
      val sigma = newFromFile(path)
    println(sigma.minimize())
    println(sigma.normalize())
      println(sigma.decompose())
    }
}
