package Common;

import P2.ExactSolverTest;
import P3.CounterexampleSearch;
import P4.ValidateEarliestFinish;
import P6.RuntimeExperiment;
import java.io.PrintWriter;
import java.nio.file.Path;

public class RunExperiments {
    public static void main(String[] args) throws Exception {
        ExactSolverTest.main(new String[0]);
        ProjectTest.main(new String[0]);
        Path directory = ExperimentSupport.output(args);
        try (PrintWriter tests = ExperimentSupport.csv(directory, "test_results.csv")) {
            ExperimentSupport.row(tests, "suite", "fixed_cases", "random_cases", "seed", "status");
            ExperimentSupport.row(tests, "ExactSolverTest", 9, 300, 4050, "passed");
            ExperimentSupport.row(tests, "ProjectTest", 4, 1000, 1234050, "passed");
        }
        CounterexampleSearch.main(args);
        ValidateEarliestFinish.main(args); // Also writes the Part 5 comparison files.
        RuntimeExperiment.main(args);
        AuditResults.main(args);
        System.out.println("All tests and Parts 3-6 experiments completed.");
    }
}
