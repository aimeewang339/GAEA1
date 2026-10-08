import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class QualityExperiments {
    public static final long SEED = 40502026L;
    public static final int PER_TYPE = 125;
    private QualityExperiments() {}

    public static void run(Path directory) throws IOException {
        ExperimentSupport.sanity();
        int[] matches = new int[4];
        double[] sums = new double[4], worst = {2, 2, 2, 2};
        String[] worstIds = new String[4], worstTypes = new String[4];
        List<List<int[]>> worstInputs = new ArrayList<>(Arrays.asList(null, null, null, null));
        int[][] typeMatches = new int[4][4];
        double[][] typeSums = new double[4][4];
        int count = 0;
        Random random = new Random(SEED);
        try (PrintWriter instances = ExperimentSupport.csv(directory, "quality_instances.csv")) {
            List<Object> header = new ArrayList<>(Arrays.asList("instance_id", "seed", "distribution", "n", "intervals", "optimal_ids", "optimum_size"));
            for (String name : ExperimentSupport.NAMES) {
                header.add(name + "_ids"); header.add(name + "_size"); header.add(name + "_ratio");
            }
            ExperimentSupport.row(instances, header.toArray());
            for (int t = 0; t < InstanceGenerator.TYPES.length; t++) {
                String type = InstanceGenerator.TYPES[t];
                for (int trial = 0; trial < PER_TYPE; trial++) {
                    String id = type + "-" + trial;
                    List<int[]> input = InstanceGenerator.generate(type, 5 + random.nextInt(8), random);
                    List<int[]> original = new ArrayList<>(input);
                    List<int[]> optimum = ExactSolver.exactSolver(input);
                    ExperimentSupport.validate(input, optimum);
                    List<Object> row = new ArrayList<>(Arrays.asList(id, SEED, type, input.size(),
                        ExperimentSupport.intervals(input), ExperimentSupport.ids(input, optimum), optimum.size()));
                    for (int a = 0; a < 4; a++) {
                        List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
                        ExperimentSupport.validate(input, selected);
                        double ratio = (double) selected.size() / optimum.size();
                        if (ratio > 1) throw new AssertionError("Selection exceeds optimum");
                        if (selected.size() == optimum.size()) { matches[a]++; typeMatches[t][a]++; }
                        sums[a] += ratio; typeSums[t][a] += ratio;
                        if (ratio < worst[a]) {
                            worst[a] = ratio; worstIds[a] = id; worstTypes[a] = type;
                            worstInputs.set(a, new ArrayList<>(input));
                        }
                        row.add(ExperimentSupport.ids(input, selected)); row.add(selected.size()); row.add(ratio);
                    }
                    if (!input.equals(original)) throw new AssertionError("Input order changed");
                    ExperimentSupport.row(instances, row.toArray());
                    count++;
                }
            }
        }
        try (PrintWriter summary = ExperimentSupport.csv(directory, "quality_summary.csv");
             PrintWriter types = ExperimentSupport.csv(directory, "quality_by_distribution.csv");
             PrintWriter failures = ExperimentSupport.csv(directory, "worst_instances.csv")) {
            ExperimentSupport.row(summary, "algorithm", "instances", "optimal_matches", "optimal_percent", "average_ratio", "worst_ratio", "worst_instance_id");
            ExperimentSupport.row(types, "distribution", "algorithm", "instances", "optimal_matches", "optimal_percent", "average_ratio");
            ExperimentSupport.row(failures, "algorithm", "instance_id", "distribution", "intervals", "selected_ids", "optimal_ids", "selected_size", "optimum_size", "ratio");
            for (int a = 0; a < 4; a++) {
                ExperimentSupport.row(summary, ExperimentSupport.NAMES[a], count, matches[a], 100.0 * matches[a] / count, sums[a] / count, worst[a], worstIds[a]);
                List<int[]> input = worstInputs.get(a), selected = ExperimentSupport.ALGORITHMS.get(a).apply(input), optimum = ExactSolver.exactSolver(input);
                ExperimentSupport.row(failures, ExperimentSupport.NAMES[a], worstIds[a], worstTypes[a], ExperimentSupport.intervals(input),
                    ExperimentSupport.ids(input, selected), ExperimentSupport.ids(input, optimum), selected.size(), optimum.size(), worst[a]);
                for (int t = 0; t < 4; t++) {
                    ExperimentSupport.row(types, InstanceGenerator.TYPES[t], ExperimentSupport.NAMES[a], PER_TYPE, typeMatches[t][a],
                        100.0 * typeMatches[t][a] / PER_TYPE, typeSums[t][a] / PER_TYPE);
                }
            }
        }
        if (matches[0] != count || matches[3] != count) throw new AssertionError("Optimal greedy disagreement");
        System.out.println("Earliest finish matched OPT on " + matches[0] + "/" + count + " instances across four distributions.");
        System.out.println("Quality summaries and complete instances saved in " + directory);
    }
}
