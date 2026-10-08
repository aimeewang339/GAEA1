# GAEA1

## Requirements

A Java Development Kit with `java` and `javac` on PATH (verified with JDK 27).
Run all commands from the repository root. No external libraries are needed.

## Compile

```sh
javac -d out src/P1/*.java src/P2/*.java src/Common/*.java src/P3/*.java src/P4/*.java src/P5/*.java src/P6/*.java
```

## Run everything

```sh
java -cp out RunExperiments
```

Runs both test suites, the counterexample search, the quality comparison,
and the runtime experiments. Generated CSV files are written to `results/`.
To use another output directory:

```sh
java -cp out RunExperiments results_new
```

## Run individual parts

| Part | Command |
| --- | --- |
| 1a Earliest finish | `java -cp out EarliestFinishTime` |
| 1b Earliest start | `java -cp out EarliestStartTime` |
| 1c Shortest duration | `java -cp out ShortestDuration` |
| 1d Latest start | `java -cp out LatestStartTime` |
| 2 Exact solver demo | `java -cp out ExactSolver` |
| 3 Counterexample search | `java -cp out CounterexampleSearch` |
| 4 Earliest finish validation | `java -cp out ValidateEarliestFinish` |
| 5 Quality comparison | `java -cp out CompareHeuristics` |
| 6 Runtime experiment | `java -cp out RuntimeExperiment` |

Parts 4 and 5 share one experiment; running either command generates both
sets of results. The Parts 3-6 commands accept an optional output-directory
argument. Search and quality data are reproducible from fixed seeds;
benchmark timings vary by run.

## Run tests

```sh
java -cp out ExactSolverTest
java -cp out ProjectTest
```

Tests cover the required sanity cases, compatibility, optimality,
deterministic selection, input preservation, and the shortest-duration
implementation's equivalence to pairwise checking.

## Change input

Edit the `jobs` list or `Arrays.asList(...)` argument in a demo's `main`
method, then recompile. Each job is an `int[]` containing `{start, finish}`
with `start < finish`. Intervals are half-open, so touching endpoints are
compatible. Demos do not read input files or command-line intervals.
Keep exact-solver inputs small because it searches subsets exhaustively.

Experiment seeds and distributions are in `src/Common/InstanceGenerator.java`
and `src/Common/QualityExperiments.java`. Search bounds are in
`src/P3/CounterexampleSearch.java`; benchmark sizes and repeats are in
`src/P6/RuntimeExperiment.java`.
