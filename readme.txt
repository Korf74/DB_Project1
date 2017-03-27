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
It does not work in the case that a functional dependency has the following form: AA -> B (repeat more than once an attribute in the left hand side).
Explanation:
 From the above functional dependency, B belong to Closure of A.
In the Algorithm 2, let's consider the For loop (line 9), count[AA -> B] > 1; then it does not satisfy the condition at line 11, so B is not included in closure.

 --------------------------

6.1 Interestingness of generate
With the set, we use the algorithm 2 in linear time.
It is because that with each iteration of the For loop (line 9), the closure increse by 1 corresponding one FD and the FD is applied once.
However it is not the case if we use the algorithm 1.


-------------------------------

6.2 Setup and methodology
-------------------------

6.3 Analysis
------------


Additional comments
===================

/* if any */

