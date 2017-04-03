package funcDep

import java.io.{BufferedReader, FileReader}

import scala.collection.immutable
import scala.collection.mutable

/**
  * Created by remi on 20/03/17.
  */
class FunctionalDependencies
  (
    private[this] val deps: mutable.Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
    ]) {

  import FunctionalDependencies._

  def this(deps: Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
    ]) =
    this(mutable.Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
      ](deps.toList: _*))

  private[this] lazy val schema: Set[Attribute] =
      deps.foldLeft(
        Set.newBuilder[Attribute]
      )((s, dep) => s ++= (dep._1 ++ dep._2)).result()

  private[this] lazy val minimized: mutable.Map[Attributes, Attributes] = {
    closures.par.filterNot{ case (x, y) =>
      y.subsetOf(computeClosure(closures - x, x))
    }.seq
  }

  private[this] lazy val normalized = {
    val min: mutable.Map[Attributes, Attributes] = minimized.clone()

    minimized.foreach{ case (x, y) =>

      val W = mutable.Set[Attribute](y.toList: _*)

      y.foreach{ a =>

        if(y.subsetOf(computeClosure(min.updated(x, (W - a).toSet), x))) {
          W -= a
        }
      }

      min.update(x, W.toSet)
    }

    min
  }

  private[this] lazy val closures: mutable.Map[Attributes, Attributes] = deps
    .par
    .map(x => (x._1, computeClosure(deps, x._1).toSet))
    .seq

  def getSchema: Set[Attribute] = schema

  def getDependencies: Map[Attributes, Attributes] = deps.toMap

  def getClosures: Map[Attributes, Attributes] = closures.toMap

  def check(X: Attributes, Y: Attributes): Boolean = Y.subsetOf(closure(X))

  def isKey(X: Attributes): Boolean = check(X, schema)

  def closure(X: Attributes): Attributes =
    if(X.isEmpty) FunctionalDependencies.algo1(this, X)
    else closures.getOrElseUpdate(X, computeClosure(deps, X).toSet)

  private[this] lazy val checkBCNF = deps
    .forall { case (x, y) =>  y.subsetOf(x) || schema.subsetOf(closure(x)) }


  def isBCNF = checkBCNF

  def decompose(): Set[FunctionalDependencies] = {
    val F = FunctionalDependencies(normalized)
    val (decomposed, invalid) = mutable.Set[FunctionalDependencies](F).span(_.isBCNF)

    while(invalid.nonEmpty) {

      val r = invalid.head

      val deps = r.getDependencies

      val fd = deps.find{case (x, y) => !y.subsetOf(x) && !r.isKey(x)}.get

      val (schema1, schema2) =
        (closure(fd._1), (r.getSchema -- closure(fd._1)) ++ fd._1)

      val split = List(
        FunctionalDependencies(
          deps
            .par
            .map{ case(x, y) =>
              (x.intersect(schema1), y.intersect(schema1))
            }
            .filter{ case (x, y) =>
              x.nonEmpty && y.nonEmpty && x.subsetOf(schema1)
            }
            .seq),
        FunctionalDependencies(
          deps
            .par
            .map{ case(x, y) =>
              (x.intersect(schema2), y.intersect(schema2))
            }
            .filter{ case (x, y) =>
              x.nonEmpty && y.nonEmpty &&  x.subsetOf(schema2)
            }
            .seq)
      )

      decomposed ++= split.filter(_.isBCNF)
      (invalid -= r) ++= split.filter(!_.isBCNF)

    }

    decomposed.toSet

  }

  def minimize(): FunctionalDependencies = FunctionalDependencies(minimized.toMap)

  def normalize(): FunctionalDependencies = FunctionalDependencies(normalized.toMap)

  override def toString: String = {
    val s = new StringBuilder

    deps.foreach{ case (from, to) =>
      from.foreach(s ++= _.toString+" ")
      s ++= "->"
      to.foreach(s ++= " "+_.toString)
      s += '\n'
    }

    s.result()
  }

  def computeClosure(fd: mutable.Map[Attributes, Attributes],
                     atts: Attributes): mutable.Set[Attribute] = {

    val count: mutable.Map[FuncDep, Int] = fd
      .map{case (w, z) => ((w, z), w.size)}

    val list: Map[Attribute, Set[FuncDep]] = fd
      .view
      .toList
      .flatMap{case (w, z) => w.map((_, (w, z)))}
      .groupBy(_._1)
      .mapValues(v => v.map(_._2).toSet)

    val Cl = mutable.Set[Attribute](atts.toList: _*)

    val update = Cl.clone()

    while(update.nonEmpty) {
      val A = update.head
      update -= A
      list.getOrElse(A, Set.empty[FuncDep]).foreach { dep =>
        if(count(dep) == 1) {
          update ++= (dep._2 -- Cl)
          Cl ++= dep._2
        } else {
          count(dep) -= 1
        }
      }
    }

    Cl
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

    fd ++= (0 until n)
      .map(
        i => (Attributes.singletonAttributes(i), Attributes.singletonAttributes(i + 1))
      )

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

        fd += (
          (k.map(Attribute(_)).toSet,
            v.filter(_ != "->").map(Attribute(_)).toSet)
          )
      }

      s = in.readLine()

    }

    fd.result()
  }

  def algo1(fd: FunctionalDependencies, atts: Attributes): immutable.Set[Attribute] =
    algo1(fd.getDependencies, atts)

  def algo1(fd: immutable.Map[Attributes, Attributes],
            atts: Attributes): immutable.Set[Attribute] = {
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

  def closure(fd: FunctionalDependencies, atts: Attributes) = {
    fd.closure(atts)
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
    fd.closure(atts)
  }

  def closure2(atts: Attributes) = {
    val fd = newFromStdin
    fd.closure(atts)
  }

  type Attributes = Set[Attribute]
  type FuncDep = (Attributes, Attributes)
}

