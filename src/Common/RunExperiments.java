public class RunExperiments {
    public static void main(String[] args) throws Exception {
        ExactSolverTest.main(new String[0]);
        ProjectTest.main(new String[0]);
        CounterexampleSearch.main(args);
        ValidateEarliestFinish.main(args); // Also writes the Part 5 comparison files.
        RuntimeExperiment.main(args);
        System.out.println("All tests and Parts 3-6 experiments completed.");
    }
}
