import funcDep.{Attribute, Attributes, FunctionalDependencies}
import org.scalameter.api._

/**
  * Created by remi on 27/03/17.
  */
object Benchmark extends Bench.OfflineReport {

  val generated: Gen[FunctionalDependencies] =
    Gen.range("size")(0, 3000, 50)
      .map(FunctionalDependencies.generate)

  val generated2: Gen[FunctionalDependencies] =
    Gen.range("size")(0, 100, 10)
      .map(FunctionalDependencies.generate)

  val X = Attributes.singletonAttributes(0)

  performance of "Closure" in {
    measure method "naive" in {
      using(generated) in { fd =>
        FunctionalDependencies.algo1(fd, X)
      }
    }

    measure method "improved" in {
      using(generated) in { fd =>
        FunctionalDependencies.algo2(fd, X)
      }
    }
  }

  performance of "Minimization" in {
    using(generated2) in { fd =>
      fd.minimize()
    }
  }

  performance of "Normalization" in {
    using(generated2) in { fd =>
      fd.normalize()
    }
  }

  performance of "Decomposition" in {
    using(generated2) in { fd =>
      fd.decompose()
    }
  }

}
