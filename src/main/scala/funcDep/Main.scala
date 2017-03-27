package funcDep

/**
  * Created by remi on 20/03/17.
  */
object Main {
  def main(args: Array[String]): Unit = {
    if(args.length < 2)
      throw new IllegalArgumentException("Not enough arguments")

    args(0) match {
      case "-naive" =>
        val atts = Set.newBuilder[Attribute]
        (2 until args.length).foreach(i => atts += Attribute(args(i)))

        args(1) match {
          case "-" => println(FunctionalDependencies.closure1(atts.result()))
          case path => println(FunctionalDependencies.closure1(atts.result(), path))
        }
      case "-improved" =>
        val atts = Set.newBuilder[Attribute]
        (2 until args.length).foreach(i => atts += Attribute(args(i)))

        args(1) match {
          case "-" => println(FunctionalDependencies.closure2(atts.result()))
          case path => println(FunctionalDependencies.closure2(atts.result(), path))
        }
      case "-generate" =>
        try {
          val n = Integer.parseInt(args(1))
          println(FunctionalDependencies.generate(n))
        } catch {
          case e: NumberFormatException => throw new IllegalArgumentException("You must enter an integer")
        }
      case "-normalize" =>
        args(1) match {
          case "-" => println(FunctionalDependencies.normalize())
          case path => println(FunctionalDependencies.normalize(path))
        }
      case "-decompose" =>
        args(1) match {
          case "-" => println(FunctionalDependencies.decompose())
          case path => println(FunctionalDependencies.decompose(path))
        }

      case _ => throw new IllegalArgumentException("wrong argument : "+args(1))
    }
  }

}
