package funcDep

import java.io.{BufferedReader, FileReader}

import scala.annotation.tailrec
import scala.collection.immutable
import scala.collection.mutable

/**
  * Created by remi on 20/03/17.
  */
class FunctionalDependencies
  (
    private val deps: mutable.Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
    ]
  ) {

  import FunctionalDependencies._

  def this(deps: Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
    ]
          ) =
    this(mutable.Map[
      FunctionalDependencies.Attributes, FunctionalDependencies.Attributes
      ](deps.toList: _*))

  private lazy val schema =
    deps
      .foldLeft(
        Set.newBuilder[Attribute]
      )((s, dep) => s ++= (dep._1 ++ dep._2)).result()

  private lazy val minimized = {
    closures.filter{ case (x, y) =>
      !y.subsetOf(computeClosure(closures - x, x))
    }
  }

  private lazy val normalized = {
    val min: mutable.Map[Attributes, Attributes] = minimized.clone()

    min.foreach{ case (x, y) =>

      val W = mutable.Set[Attribute](y.toList: _*)

      val G = min.clone()

      y.foreach{ a =>
        G.update(x, (W - a).toSet)

        if(y.subsetOf(computeClosure(G, x))) {
          W -= a
        }
      }

      min.update(x, W.toSet)
    }

    min
  }

  private lazy val closures: mutable.Map[Attributes, Attributes] = deps
    .map(x => (x._1, computeClosure(deps, x._1).toSet))

  def update(x: Attributes, y: Attributes): Unit = deps.update(x, y)

  def getSchema: Set[Attribute] = schema

  def getDependencies: Map[Attributes, Attributes] = deps.toMap

  def getClosures: Map[Attributes, Attributes] = closures.toMap

  def setClosure(x: Attributes, y: Attributes): Unit = closures.update(x, y)

  def check(X: Attributes, Y: Attributes): Boolean = Y.subsetOf(closure(X))

  def isKey(X: Attributes): Boolean = check(X, schema)

  def closure(X: Attributes): Attributes =
    closures.getOrElseUpdate(X, computeClosure(deps, X).toSet)

  def isBCNF(R: Attributes): Boolean = {
    deps
      .filter { case (x, y) => x.subsetOf(R) && y.subsetOf(R) }
      .forall { case (x, y) =>  y.subsetOf(x) || R.subsetOf(closure(x)) }
  }

  def decompose(): Set[Attributes] = decompose(schema)

  private def decompose(schema: Attributes) = {
    val F = FunctionalDependencies(normalized)
    val R = mutable.Set[Attributes]()
    val decomposed = Set.newBuilder[Attributes]

    if(isBCNF(schema)) {
      decomposed += schema
    } else {
      R += schema
    }

// supress bcnfs to not retest them
    while(R.nonEmpty) {

      val r = R.head

      val X = F.getDependencies
        .find{ case (x, y) => x.subsetOf(r) && y.subsetOf(r) &&
          !FunctionalDependencies.isKey(F, x, r)
        }
        .get._1

      val X_+ = closure(X)


      val (newR, done) = mutable.Set[Attributes](X_+, (r -- X_+) ++ X)
        .span(!isBCNF(_))

      (R -= r) ++= newR
      decomposed ++= done

    }

    decomposed.result()
  }

  def minimize(): Map[Attributes, Attributes] = minimized.toMap

  def normalize(): Map[Attributes, Attributes] = normalized.toMap

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

  def computeClosure(fd: mutable.Map[Attributes, Attributes],
                     atts: Attributes): mutable.Set[Attribute] = {

    val count: mutable.Map[FuncDep, Int] = fd
      .map{case (w, z) => ((w, z), w.size)}

    val list: Map[Attribute, Set[FuncDep]] = fd
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

  def isBCNF(sigma: FunctionalDependencies, R: Attributes) = sigma.isBCNF(R)

  def isKey(sigma: FunctionalDependencies, X: Attributes, R: Attributes) =
    R.subsetOf(closure(sigma, X))

  def schema(sigma: FunctionalDependencies) = sigma.schema

  def check(sigma: FunctionalDependencies, X: Attributes, Y: Attributes): Boolean =
    sigma.check(X, Y)

  private def check(sigma: mutable.Map[Attributes, Attributes],
                    X: Attributes, Y: Attributes): Boolean =
    FunctionalDependencies(sigma).check(X, Y)

  private def check(sigma: mutable.Map[Attributes, Attributes],
                    X: Attributes, Y: Attributes,
                    closures: mutable.Map[Attributes, Attributes]) = {
    val cl = closures.getOrElseUpdate(X, algo2(sigma.toMap, X))
    Y.subsetOf(cl)
  }

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

  def algo2(fd: FunctionalDependencies, atts: Attributes): Set[Attribute] =
    algo2(fd.getDependencies, atts)

  def algo2(fd: Map[Attributes, Attributes], atts: Attributes): Set[Attribute] = {

    val count: mutable.Map[FuncDep, Int] =
      mutable.Map[FuncDep, Int](fd.map{case (w, z) => ((w, z), w.size)}.toList: _*)

    val list: Map[Attribute, Set[FuncDep]] = fd
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

