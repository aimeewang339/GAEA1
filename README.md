# GAEA1

## Requirements and downloads

This project uses only Java's standard library. No Python, Maven, Gradle,
third-party JARs, or separate scripts are required.

- Install a **JDK 21 or newer**, not just a JRE. [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jdk) is a supported choice. On Windows, enable the installer's PATH and JAVA_HOME options, then reopen your terminal. Alternatively: `winget install --id EclipseAdoptium.Temurin.21.JDK --exact`.
- For VS Code, install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack), or run `code --install-extension vscjava.vscode-java-pack`.

Check installation with `java -version` and `javac -version`. Both must be
21 or newer. Compilation targets Java 21; the entry points were tested on
Java 21 and Java 27. The existing JDK and Java extensions on this computer
were sufficient; no additional software was downloaded for this update.

## Run in VS Code

1. Open the **inner GAEA1 folder containing this README, `src`, and `.vscode`** using File > Open Folder.
2. Wait for Java project loading. Open Run and Debug (`Ctrl+Shift+D`).
3. Choose **All tests and experiments**, then press `Ctrl+F5` to run without debugging, or `F5` to debug. Other configurations run each demo, experiment, or test separately.

The configurations compile the complete project first and use the repository
root as the working directory. `Ctrl+Shift+B` also compiles everything.
You can use **Run Java** above a `main` method. Code Runner's **Run Code**
compiles an isolated file and is not the correct launcher for these packages.

If VS Code still shows old errors, run **Java: Clean Java Language Server
Workspace** from the Command Palette and reload. Use **Java: Configure Java
Runtime** to select an installed JDK 21 or newer if needed. See the official
[Java project setup](https://code.visualstudio.com/docs/java/java-project) and
[Run and Debug guide](https://code.visualstudio.com/docs/java/java-debugging).

## Run from a terminal

Run these commands from the repository root:

```sh
javac --release 21 -d out "@sources.txt"
java -cp out Common.RunExperiments
```

`sources.txt` lists every Java source file; add new files there if extending
the project. The full run executes both test suites, the search, the quality
comparison, the benchmarks, and an independent audit of the resulting CSVs.
It writes to `results/`. To keep the included evidence unchanged:

```sh
java -cp out Common.RunExperiments results_new
```

## Individual entry points

After compilation, use `java -cp out` followed by one of these class names:

| Program | Class name |
| --- | --- |
| 1a Earliest finish demo | `P1.EarliestFinishTime` |
| 1b Earliest start demo | `P1.EarliestStartTime` |
| 1c Shortest duration demo | `P1.ShortestDuration` |
| 1d Latest start demo | `P1.LatestStartTime` |
| 2 Exact solver demo | `P2.ExactSolver` |
| Exact solver tests | `P2.ExactSolverTest` |
| 3 Counterexample search | `P3.CounterexampleSearch` |
| 4 Earliest finish validation | `P4.ValidateEarliestFinish` |
| 5 Quality comparison | `P5.CompareHeuristics` |
| 6 Runtime experiment | `P6.RuntimeExperiment` |
| Project tests | `Common.ProjectTest` |
| Audit saved results | `Common.AuditResults` |
| Complete run | `Common.RunExperiments` |

Example: `java -cp out P3.CounterexampleSearch`.
Parts 4 and 5 share a dataset; running either generates both sets of outputs.
Experiment and audit entry points accept one optional results-directory
argument. Run the full suite first when auditing a new directory.

`ExperimentSupport`, `InstanceGenerator`, and `QualityExperiments` are shared
helper classes, not independent programs. They deliberately have no `main`;
the runnable entry points above call them.

## Included experimental evidence

The `results/` directory is retained in the project, including:

| File | Contents |
| --- | --- |
| `test_results.csv` | Both test suites, case counts, seeds, and pass status |
| `search_instances.csv` | All 7,546 searched inputs, optimums, and heuristic selections |
| `search_summary.csv` | Search bounds, counts, and failure status |
| `counterexamples.csv` | Automatically found failures and both selected schedules |
| `counterexample_timeline.svg` | Java-generated visualization of a searched failure |
| `quality_instances.csv` | All 500 inputs and every selected schedule, count, and ratio |
| `quality_summary.csv` | Overall optimal percentages, average ratios, and worst ratios |
| `quality_by_distribution.csv` | Results for each of the four distributions |
| `worst_instances.csv` | Complete worst inputs, selected jobs, and optimums |
| `runtime_samples.csv` | All 112 measured repetitions |
| `runtime_summary.csv` | Median, minimum, and maximum timing per size and strategy |
| `runtime_environment.csv` | Java/OS details, benchmark seeds, and warmup method |

Job IDs such as `J0` refer to the intervals in that row, encoded as
`J0:[start,finish)`. The audit independently enumerates subsets to verify all
500 quality optimums and 7,546 search optimums, and checks saved selections,
statistics, and timing medians. Tests support implementation correctness;
experimental agreement alone is not a mathematical proof of optimality.

## Change input and reproduce experiments

Demo input is defined in each `main` method as `{start, finish}` arrays with
`start < finish`; edit and recompile. Demos do not accept interval files.
Keep exact-solver input small because its search is exponential.

Quality uses seed 40502026 and 125 inputs per distribution, with 5-12 jobs.
Search enumerates distinct sets of 1-4 intervals over integer endpoints 0-6.
Benchmarks use seed 60602026 + n, n = 100, 1,000, 10,000, 100,000, and seven
measured repetitions after warmup. Search and quality CSVs reproduce exactly;
elapsed timings vary by JVM and computer.

Generator settings are in `src/Common/InstanceGenerator.java` and
`src/Common/QualityExperiments.java`. Search bounds are in
`src/P3/CounterexampleSearch.java`; benchmark settings are in
`src/P6/RuntimeExperiment.java`.
