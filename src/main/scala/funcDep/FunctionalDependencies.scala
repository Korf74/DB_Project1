package funcDep

import java.io.{FileReader, BufferedReader}
import scala.collection.immutable
import scala.collection.mutable

/**
  * Created by remi on 20/03/17.
  */
class FunctionalDependencies(private val deps: Map[FunctionalDependencies.Attributes, FunctionalDependencies.Attributes]) {

  import FunctionalDependencies._

  def getDependencies = deps

  def schema = deps.foldLeft(Set.newBuilder[Attribute])((s, dep) => s ++= (dep._1 ++ dep._2)).result()

  def check(X: Attributes, Y: Attributes) = Y.subsetOf(closure(X))

  def isKey(X: Attributes) = check(X, schema)

  def closure(X: Attributes) = FunctionalDependencies.closure(this, X)

  def isBCNF(R: Attributes) = {
    deps
      .filter { case (x, y) => x.subsetOf(R) && y.subsetOf(R) }
      .forall { case (x, y) =>  y.subsetOf(x) || FunctionalDependencies.isKey(this, x, R) }
  }

  def decompose(): Set[Attributes] = decompose(schema)

  private def decompose(schema: Attributes) = {
    val F = normalize()
    val R = mutable.Set[Attributes](schema)

    while(R.exists(!F.isBCNF(_))) {

      val r = R.find(!isBCNF(_)).get

      val X = F.getDependencies
        .find{ case (x, y) => x.subsetOf(r) && y.subsetOf(r) && !FunctionalDependencies.isKey(F, x, r) }
        .get._1

      val X_+ = FunctionalDependencies.closure(F, X)

      println("r : "+r)
      println("X :"+X)

      val newR = mutable.Set[Attributes](X_+, (r -- X_+) ++ X)

      (R -= r) ++= newR

    }

    R.toSet
  }

  def minimize() = {
    val G: mutable.Map[Attributes, Attributes] = mutable.HashMap[Attributes, Attributes](deps.toList: _*)
      .map(dep => (dep._1, closure(dep._1)))

    for {
      (x, x_+) <- G
    } yield {
      if(FunctionalDependencies.check(G - (x, x_+), x, x_+)) {
        G -= x
      }
    }
    FunctionalDependencies(G)
  }

  def reduce() = {
    val min: mutable.Map[Attributes, Attributes] = mutable.HashMap[Attributes, Attributes](deps.toList: _*)

    min.foreach{ case (x, y) =>
        val W = mutable.Set[Attribute](y.toList: _*)

        y.foreach{ a =>
          val G = (min - (x, y)) + ((x, (W - a).toSet))
          if(FunctionalDependencies.check(G, x, y)) {
            W -= a
          }
        }

        min -= (x, y)
        min += ((x, W.toSet))
    }

    FunctionalDependencies(min)
  }

  def normalize() = minimize().reduce()

  override def toString: String = {
    val s = new StringBuilder

    deps.foreach{ case (from, to) =>
      from.foreach(s ++= _.toString()+" ")
      s ++= "->"
      to.foreach(s ++= " "+_.toString())
      s += '\n'
    }

    s.result()
  }

}

object FunctionalDependencies {

  private class Builder {

    private val deps = mutable.HashMap[Attributes, Attributes]()

    def +=(dep: (Attributes, Attributes)) = {
      val old = deps.getOrElse(dep._1, Set.empty[Attribute])
      deps += ((dep._1, old ++ dep._2))
    }

    def ++=(xs: TraversableOnce[(Attributes, Attributes)]) = xs.foreach(+=)

    def result() = {
      new FunctionalDependencies(deps.toMap)
    }

  }

  private def newBuilder() = new Builder()

  def apply(deps: mutable.Map[Attributes, Attributes]) =
    new FunctionalDependencies(deps.toMap)

  def apply(deps: Map[Attributes, Attributes]) = new FunctionalDependencies(deps)

  def isBCNF(sigma: FunctionalDependencies, R: Attributes) = sigma.isBCNF(R)

  def isKey(sigma: FunctionalDependencies, X: Attributes, R: Attributes) = R.subsetOf(closure(sigma, X))

  def schema(sigma: FunctionalDependencies) = sigma.schema

  def check(sigma: FunctionalDependencies, X: Attributes, Y: Attributes): Boolean = sigma.check(X, Y)

  private def check(sigma: mutable.Map[Attributes, Attributes], X: Attributes, Y: Attributes) =
    Y.subsetOf(algo2(sigma.toMap,  X))

  def normalize(path: String) = {
    val sigma = newFromFile(path)
    sigma.normalize()
  }

  def normalize() = {
    val sigma = newFromStdin
    sigma.normalize()
  }

  def decompose(path: String) = {
    val sigma = newFromFile(path)
    sigma.decompose()
  }

  def decompose() = {
    val sigma = newFromStdin
    sigma.decompose()
  }

  def generate(n: Int) = {
    val fd = newBuilder()

    fd ++= (0 until n).map(i => (Attributes.singletonAttributes(i), Attributes.singletonAttributes(i + 1)))

    fd.result()
  }

  def newFromStdin = {
    val in = scala.io.StdIn

    val fd = newBuilder()

    var s: String = in.readLine()

    while(s != null) {
      if (!s.isEmpty && s.charAt(0) != '#') {
        val (k, v) = s.split(" ").filter(!_.isEmpty).span(_ != "->")

        fd += ((k.map(Attribute(_)).toSet, v.filter(_ != "->").map(Attribute(_)).toSet))
      }

      s = in.readLine()

    }

    fd.result()

  }

  def newFromFile(name: String) = {
    val in = new BufferedReader(new FileReader(name))

    val fd = newBuilder()

    var s: String = in.readLine()

    while(s != null) {
      if (!s.isEmpty && s.charAt(0) != '#') {
        val (k, v) = s.split(" ").filter(!_.isEmpty).span(_ != "->")

        fd += ((k.map(new Attribute(_)).toSet, v.filter(_ != "->").map(new Attribute(_)).toSet))
      }

      s = in.readLine()

    }

    fd.result()
  }

  def algo1(fd: FunctionalDependencies, atts: Attributes): immutable.Set[Attribute] =
    algo1(fd.getDependencies, atts)

  def algo1(fd: immutable.Map[Attributes, Attributes], atts: Attributes): immutable.Set[Attribute] = {
    val Cl = mutable.HashSet[Attribute](atts.toList: _*)

    var done = false

    while(!done) {
      done = true
      fd.foreach { case (w, z) =>
        if(w.subsetOf(Cl) && !z.subsetOf(Cl)) {
          Cl ++= z
          done = false
        }
      }
    }

    Cl.toSet
  }

  def algo2(fd: FunctionalDependencies, atts: Attributes): immutable.Set[Attribute] =
    algo2(fd.getDependencies, atts)

  def algo2(fd: immutable.Map[Attributes, Attributes], atts: Attributes): immutable.Set[Attribute] = {

    val count: mutable.HashMap[FuncDep, Int] = new mutable.HashMap[FuncDep, Int]
    count ++= fd.map{case (w, z) => ((w, z), w.size)}

    val list: Map[Attribute, Set[FuncDep]] = fd
      .toList
      .flatMap{case (w, z) => w.map((_, (w, z)))}
      .groupBy(_._1)
      .mapValues(v => v.map(_._2).toSet)

    val Cl = mutable.HashSet[Attribute](atts.toList: _*)

    val update = mutable.HashSet[Attribute](atts.toList: _*)

    while(update.nonEmpty) {
      val A = update.head
      update -= A
      list.getOrElse(A, Set.empty[FuncDep]).foreach { case dep =>
        count(dep) -= 1
        if(count(dep) == 0) {
          update ++= (dep._2 -- Cl)
          Cl ++= dep._2
        }
      }
    }

    Cl.toSet
  }

  def closure(fd: FunctionalDependencies, atts: Attributes) = {
    algo2(fd, atts)
  }

  def closure1(atts: Attributes, path: String) =  {
    val fd = newFromFile(path)
    algo1(fd, atts)
  }

  def closure1(atts: Attributes) = {
    val fd = newFromStdin
    algo1(fd, atts)
  }

  def closure2(atts: Attributes, path: String) =  {
    val fd = newFromFile(path)
    algo2(fd, atts)
  }

  def closure2(atts: Attributes) = {
    val fd = newFromStdin
    algo2(fd, atts)
  }

  type Attributes = Set[Attribute]
  type FuncDep = (Attributes, Attributes)
}

