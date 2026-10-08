import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CounterexampleSearch {
    private long checked;
    private final List<List<int[]>> examples = new ArrayList<>(Arrays.asList(null, null, null, null));

    private void enumerate(List<int[]> universe, int next, int remaining, List<int[]> input) {
        if (remaining == 0) {
            checked++;
            List<int[]> optimum = ExactSolver.exactSolver(input);
            for (int a = 0; a < 4; a++) {
                List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
                ExperimentSupport.validate(input, selected);
                if (selected.size() > optimum.size()) throw new AssertionError("Oracle disagreement");
                if (selected.size() < optimum.size() && examples.get(a) == null) {
                    examples.set(a, new ArrayList<>(input));
                }
            }
            return;
        }
        for (int i = next; i <= universe.size() - remaining; i++) {
            input.add(universe.get(i));
            enumerate(universe, i + 1, remaining - 1, input);
            input.remove(input.size() - 1);
        }
    }

    public static void main(String[] args) throws IOException {
        Path directory = ExperimentSupport.output(args);
        ExperimentSupport.sanity();
        CounterexampleSearch search = new CounterexampleSearch();
        List<int[]> universe = new ArrayList<>();
        for (int start = 0; start < 6; start++) {
            for (int finish = start + 1; finish <= 6; finish++) universe.add(new int[]{start, finish});
        }
        for (int n = 1; n <= 4; n++) search.enumerate(universe, 0, n, new ArrayList<>());
        try (PrintWriter summary = ExperimentSupport.csv(directory, "search_summary.csv");
             PrintWriter examples = ExperimentSupport.csv(directory, "counterexamples.csv")) {
            ExperimentSupport.row(summary, "algorithm", "instances_checked", "endpoint_min", "endpoint_max", "max_jobs", "counterexample_found");
            ExperimentSupport.row(examples, "algorithm", "intervals", "selected_ids", "optimal_ids", "selected_size", "optimum_size");
            for (int a = 0; a < 4; a++) {
                List<int[]> input = search.examples.get(a);
                ExperimentSupport.row(summary, ExperimentSupport.NAMES[a], search.checked, 0, 6, 4, input != null);
                if (input != null) {
                    List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
                    List<int[]> optimum = ExactSolver.exactSolver(input);
                    ExperimentSupport.row(examples, ExperimentSupport.NAMES[a], ExperimentSupport.intervals(input),
                        ExperimentSupport.ids(input, selected), ExperimentSupport.ids(input, optimum), selected.size(), optimum.size());
                }
            }
        }
        if (search.examples.get(1) == null || search.examples.get(2) == null) throw new AssertionError("Required failures not found");
        System.out.println("Search checked " + search.checked + " instances; required counterexamples saved.");
    }
}
