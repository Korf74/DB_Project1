package funcDep

/**
  * Created by remi on 20/03/17.
  */
object Attributes {

  def empty = Set.empty[Attribute]

  def apply(names: String*): Set[Attribute] = names.foldLeft(Set.newBuilder[Attribute])((b, s) => b += Attribute(s)).result()

  def newFromStringSet(atts: Set[String]) = atts.map(new Attribute(_))

  def newFromIntSet(atts: Set[Int]) = atts.map(new Attribute(_))

  def singletonAttributes(n: Int) = Set(new Attribute(n))

  def singletonAttributes(name: String) = Set(new Attribute(name))

}

class Attribute(private val name: String) {

  def this(n: Int) = this(n.toString)

  override def toString(): String = name

  override def equals(other: Any) = other.isInstanceOf[Attribute] && name == other.asInstanceOf[Attribute].getName()

  override def hashCode(): Int = name.hashCode

  def getName() = name

}

object Attribute {

  def apply(name: String) = new Attribute(name)

  def apply(n: Int) = new Attribute(n)

}
