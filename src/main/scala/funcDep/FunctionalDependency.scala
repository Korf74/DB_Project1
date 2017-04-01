/*package funcDep

import funcDep.FunctionalDependencies.Attributes

/**
  * Created by remi on 29/03/17.
  */
class FunctionalDependency(val from: Attributes, to: Attributes) {

  // private
  private lazy val str = from+" -> "+to


  // public
  def get: (Attributes, Attributes) = (from, to)
  def first: Attributes = from
  def second: Attributes = to
  def _1: Attributes = first
  def _2: Attributes = second

  override def toString: Unit = str

}

class FunctionalDependencies2(private val fds: Set[FunctionalDependency]) {

  // private
  private lazy val minimized: Set[FunctionalDependency] = ???
  private lazy val normalized: Set[FunctionalDependency] = ???
  private lazy val closures: Set[FunctionalDependency] = ???

  // public
  def minimize: Set[FunctionalDependency] = minimized
  def normalize: Set[FunctionalDependency] = normalized
  def closure(atts: Attributes): Attributes = closures.find(_._1 == atts).getOrElse((Set.empty[Attribute], Set.empty[Attribute]))
}
*/