import funcDep.FunctionalDependencies._
import funcDep._
import org.scalatest._

/**
  * Created by remi on 20/03/17.
  */
class tests extends FunSuite {

  lazy val generates: List[FunctionalDependencies] =
    List(1, 10, 50, 100/*, 500, 1000*/).map(FunctionalDependencies.generate)

  val generate200: FunctionalDependencies = FunctionalDependencies.generate(200)

  lazy val examples: List[FunctionalDependencies] = List(
    "ex.CC1.txt",
    "ex.TD3.txt",
    "CCF2015P.txt"
  ).map(s => FunctionalDependencies.newFromFile(
    getClass.getResource(s).getPath)
  )

  val path = getClass.getResource("/ex.CC1.txt").getPath
  val path1 = getClass.getResource("/ex.TD3.txt").getPath
  val path2 = getClass.getResource("/generate100.txt").getPath
  val path3 = getClass.getResource("/generate5.txt").getPath
  val path4 = getClass.getResource("/generate500_shuffle.txt").getPath
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
    generates.foreach { fd =>
      assert(fd.minimize().forall {
        case (x, y) => y == fd.closure(x)
      })
    }
  }

  test("normalization") {
    generates.foreach { fd =>
      val normalized = fd.normalize()
      assert(normalized.forall {
        case(x, y) =>
          Integer.parseInt(x.head.getName()) == Integer.parseInt(y.head.getName()) - 1
      })
    }
  }

  test("decompose") {
    generates.foreach { fd =>
      val decomposed = fd.decompose()
      assert(decomposed.forall(fd.isBCNF))
      assert(decomposed.reduceLeft((a, b) => a ++ b) == fd.getSchema)
    }
  }

}
