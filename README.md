# GAEA1

## Part 1: Interval scheduling

All four Problem 1 implementations are in `src/P1/`.

Java implementations use a `List<int[]>`, where each job is `{start, finish}`
with `start < finish`. Methods return the selected jobs and leave the input
list order unchanged. Intervals are half-open, so touching endpoints are compatible.

Compile and run with a JDK (verified with JDK 27):

```sh
javac -d out src/P1/*.java
java -cp out EarliestFinishTime
java -cp out EarliestStartTime
java -cp out ShortestDuration
java -cp out LatestStartTime
```

### 1a: Earliest finish time

Sort by increasing finish time, then increasing start time. Identical intervals
retain input order, making ties deterministic. Accept each job if its start is
at least the finish of the previously selected job. Results are returned in
chronological order. Sorting costs O(n log n), and scanning costs O(n), with
O(n) extra space.

The demo uses `(0, 10), (1, 2), (2, 3), (3, 4), (4, 5)` and selects
`(1, 2), (2, 3), (3, 4), (4, 5)`, an optimal solution of four jobs.

### 1b: Earliest start time

Sort by increasing start time, then increasing finish time. Identical intervals
retain input order, making ties deterministic. Accept each job if its start is
at least the finish of the previously selected job. Results are returned in
chronological order. Sorting costs O(n log n), and scanning costs O(n), with
O(n) extra space.

The demo uses `(0, 10), (1, 2), (2, 3), (3, 4), (4, 5)` and selects only
`(0, 10)`. The optimum selects the four shorter jobs, demonstrating that
earliest start is not always optimal.

### 1c: Shortest duration

Sort by duration, then increasing start, then increasing finish. Identical
intervals retain input order. Duration uses `long` to avoid integer overflow.
Accept each job only if it does not overlap any selected job. Checking only
the last finish would be incorrect because duration order is not chronological.
Results are in selection order. Sorting costs O(n log n); compatibility checks
cost O(n^2) in the worst case, with O(n) extra space.

The demo uses `(2, 4), (0, 3), (3, 6)` and selects only `(2, 4)`.
The optimum selects `(0, 3), (3, 6)`, demonstrating that this rule can fail.

### 1d: Latest start time

Choose the compatible job with the latest start, scheduling backward. Sort by
decreasing start, then decreasing finish; identical intervals retain input order.
Accept a job if its finish is at most the start of the previously selected job.
Reverse the result to return chronological order. Runtime is O(n log n), with
O(n) extra space. The demo selects all four shorter jobs.

This rule uses a different priority from A-C and is optimal. Replace the last
job in an optimal schedule with a job having the latest start. Its start is
at least the replaced job's start, so it remains compatible with every earlier
job. Repeat the argument on jobs finishing before the selected start; induction
proves optimality. Experimental agreement alone would not prove this claim.

## Part 2: Exact solver

The solver and its tests are in `src/P2/`. `ExactSolver.exactSolver(jobs)`
uses the same input format as Part 1 and returns an optimal set of jobs in
chronological order without changing the input list.

Sort by increasing start, then increasing finish, retaining input order for
identical jobs. Recursively explore both including and skipping each job.
Include a job only when its start is at least the last selected finish.
Keep the largest complete selection; ties keep the first maximum found,
with the include branch explored before the skip branch.

Every compatible subset has an include/skip path in the search. Sorting by
start makes checking the last selected finish sufficient to ensure compatibility.
Only incompatible include branches are discarded, so choosing the largest
selection over the remaining paths always returns an optimum.

Sorting costs O(n log n). The search has O(2^n) nodes in the worst case;
copying improved solutions costs up to O(n) per update, giving an O(n * 2^n)
upper bound overall. Auxiliary space is O(n) for the sorted list, recursion,
current selection, and best selection. Exponential growth makes this practical
only for small instances; use greedy algorithms for large runtime experiments.

Compile and run the demo and tests:

```sh
javac -d out src/P1/*.java src/P2/*.java
java -cp out ExactSolver
java -cp out ExactSolverTest
```

The demo returns `(1, 2), (2, 3), (3, 4), (4, 5)`, an optimum of four jobs.
Tests include both assignment sanity examples, empty input, touching endpoints,
overlapping jobs, duplicates, ties, and negative/extreme times. They also compare
300 small random instances (seed 4050, at most 10 jobs each) against an independent
subset enumeration oracle that checks every pair without sorting.

AI assistance: Codex helped implement parts 1b-1d and Part 2, document their ordering and
complexity, fix the part 1a class-name capitalization, and run sanity checks.
