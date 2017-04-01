DBDM : Closure Algorithm for Functional Dependencies
====================================================

LASTNAME#1 FirstName#1
LASTNAME#2 FirstName#2


Content
=======

readme.txt : this file
results.csv : raw results
plot.png : figure depicting results

/* to be completed with the list of your source files, */
/* feel free to add relevant supplementary material */
/* prevent yourselves from adding binaries or the originaly provided examples */


Open questions
==============

4.1 Justifications of data structures
-------------------------------------

4.2 Strategy for Choose A
-------------------------

4.3 Find the bug
It does not work in the case that a functional dependency has the lelf hand side which is empty. The closure of empty set is whole set of attributes but the algorithm do not run the dependency.

Moreover, the algorithm does not work well if a functional dependency has the following form: AA -> B (repeat more than once an attribute in the left hand side).
 From the above functional dependency, B belong to Closure of A.
In the Algorithm 2, let's consider the For loop (line 9), count[AA -> B] > 1; then it does not satisfy the condition at line 11, so B is not included in closure.

 --------------------------

6.1 Interestingness of generate
With the set, Algorithm 2 works much effectively (in linear time) than Algorithm 1 (not in linear time).
Explanation:
With Algorithm 2: each dependency is read only exactly once that it is checked at line 9.
However, if using Algorithm 1, a dependency could be read many times. Let's consider the For loop at line 5, it checks every funtional dependency whose lelf hand side in is Closure but there is only one of them that satisfies the condition at line 6. 
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


-------------------------------

6.2 Setup and methodology
-------------------------

6.3 Analysis
------------


Additional comments
===================

/* if any */

