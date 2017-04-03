DBDM : Closure Algorithm for Functional Dependencies
====================================================

COUDERT Rémi
NGUYEN Ha

NOTE
====
The code is written in Scala. We provide to you a jar file you can run with java -jar and a script (closure) that automatically does this.
If you want to run the unit tests you'll have to install sbt, then do 'sbt test'.

What is assumed :
- You input has the same form as the example files
- The attributes given as arguments are separated by spaces

When using standard input, enter you dependencies one by one sperated by new lines then type CTRL+D to finish you input.


Content
=======

readme.txt : this file
benchmark/index.html : Benchmark results taking the form of an interactive plot, plotting running time against generated size. I've added some running times for minimization, normalization and decomposition but they are not really representative since they are run at max with generate100. Naive and improved algorithms for closures are tested until a size of 3000.

src/test/* : Unit tests
src/bench/* : Benchmarks
src/main/scala/funcDep/* : The actual code


Open questions
==============

4.1 Justifications of data structures
-------------------------------------

NOTE : Scala classes can be classes or objects, objects with the same name as classes are their companion object, it's meant to hold global variables and static functions.

'Attribute' is a class containing the attribute name and a way to print it.
'Attributes' is a type used as a simplification for Set[Attribute] and is also used as an helper Object in 'Attribute.scala'

The datastructure used is a class named 'FunctionalDependencies' that handles all functions we need, it has a companion object for static functions. This class stores internally a Map Attributes -> Attributes.
Maps in Scala are more or less the equivalent of Java's HashMaps, thus having constant time lookup, add, remove which suits our need well in addition of being a good theoretical representation
of Functional dependencies.


4.2 Strategy for Choose A
-------------------------
We simply choose the first one the Set gives us.


4.3 Find the bug
---------------------------
The cornercase happens in the case of a functional dependency with an empty left-handside and a non-empty right-handside e.g ' -> A', in this case the algorithm returns an empty set but it should return the right-handside. We took care of this case in the code by simply calling the first version of the algorithm.

There is a problems as well if the implementation is not done with sets and the left-handside contains doublons, e.g. AA -> B, which will not yield a correct result. Since sets do not have duplicates, a correct implementation should not make this case arise.


6.1 Interestingness of generate
------------------------------

With sets given by generate, Algorithm 2 works much more efficiently (linear time) than Algorithm 1 (n^2).

Explanation:
With Algorithm 2: each dependency is read only exactly once that it is checked at line 9.
However, when using Algorithm 1, a dependency could be read many times. Let's consider the For loop at line 5, it checks every functional dependency whose left hand side in is Closure but there is only one of them that satisfies the condition at line 6

Counter-example: case n=3, 
	Functional dependencies: 0 -> 1, 1 -> 2, 2 -> 3. Find  Cl(0)

	Algorithm 2: closure = 0, update = 0
			While loop: 1. choose 0, update = empty,
				       For loop: count[0 -> 1] = 0; update = 1, closure = {0, 1}
				    2. choose 1, update = empty,
				       For loop: count[1 -> 2] = 0; update = 2, closure = {0, 1, 2}
  				    3. choose 2, update = empty,
				       For loop: count[2 -> 3] = 0; update = 3, closure = {0, 1, 2, 3}
                       
	Algorithm 1: Cl = 0, 
		       While loop:  1. check all dependencies, 0 -> 1 satisfies line 6, Cl = {0, 1}
				    2. check all dependencies, 1 -> 2 satisfies line 6, Cl = {0, 1, 2}
				    3. check all dependencies, 2 -> 3 satisfies line 6, Cl = {0, 1, 2, 3}

In addition, an interesting fact is that functional dependencies generated like this are already normalized, moreover the closure of i is {j | j >= i, j <= n} for a generate n, which makes it really hard to compute as well when looking for normalized form (even if it's already in normal form, we could have chosen to check if the database is already normalized before computing its normalization).


6.2 Setup and methodology
-------------------------
The library used for performance comparison is called ScalaMeter (http://scalameter.github.io/)

The benchmarks are configured to test the two algorithms for functional dependencies generated between 0 and 3000, with an step of 5 each time.
The library uses a 'warmup' that permits to detect steady-states in the JVM garbage collector, once done it tests X times on each input where X is given (here 36, by default).
The outliers are suppressed automatically and the output uses the mean and variance of each test. The test can be more precise but I chose to stay simple.



6.3 Analysis
------------
The output of the benchmark can be seen in target/benchmarks/report/index.html, ScalaMeter generates an html file containing all needed information.
The output takes the form of a simple graph but you can look at its histogram form. You can choose to see only the input you want.
You can chose to show standard deviation.

What we can see from these datas is the following, at first Algorithm 1 is better (for little values), but as the input size grows there is a clear tendancy for it to be non-linear while the second Algorithm is almost linear.



Additional comments
===================

