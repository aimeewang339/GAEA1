package P3;

import Common.ExperimentSupport;
import P2.ExactSolver;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CounterexampleSearch {
    private long checked;
    private PrintWriter instanceResults;
    private final List<List<int[]>> examples = new ArrayList<>(Arrays.asList(null, null, null, null));

    private void enumerate(List<int[]> universe, int next, int remaining, List<int[]> input) {
        if (remaining == 0) {
            checked++;
            List<int[]> optimum = ExactSolver.exactSolver(input);
            List<Object> evidence = new ArrayList<>(Arrays.asList(checked, ExperimentSupport.intervals(input),
                ExperimentSupport.ids(input, optimum), optimum.size()));
            for (int a = 0; a < 4; a++) {
                List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
                ExperimentSupport.validate(input, selected);
                evidence.add(ExperimentSupport.ids(input, selected));
                evidence.add(selected.size());
                if (selected.size() > optimum.size()) throw new AssertionError("Oracle disagreement");
                if (selected.size() < optimum.size() && examples.get(a) == null) {
                    examples.set(a, new ArrayList<>(input));
                }
            }
            ExperimentSupport.row(instanceResults, evidence.toArray());
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
        try (PrintWriter instances = ExperimentSupport.csv(directory, "search_instances.csv")) {
            search.instanceResults = instances;
            List<Object> header = new ArrayList<>(Arrays.asList("instance_id", "intervals", "optimal_ids", "optimum_size"));
            for (String name : ExperimentSupport.NAMES) { header.add(name + "_ids"); header.add(name + "_size"); }
            ExperimentSupport.row(instances, header.toArray());
            for (int n = 1; n <= 4; n++) search.enumerate(universe, 0, n, new ArrayList<>());
        }
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
        writeTimeline(directory, search.examples.get(2));
        System.out.println("Search checked " + search.checked + " instances; required counterexamples saved.");
    }

    private static void writeTimeline(Path directory, List<int[]> input) throws IOException {
        List<int[]> greedy = ExperimentSupport.ALGORITHMS.get(2).apply(input);
        List<int[]> optimum = ExactSolver.exactSolver(input);
        int maximum = input.stream().mapToInt(job -> job[1]).max().orElse(1);
        StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"300\" viewBox=\"0 0 800 300\">\n");
        svg.append("<rect width=\"800\" height=\"300\" fill=\"white\"/>\n");
        svg.append("<text x=\"20\" y=\"25\" font-family=\"Arial\" font-size=\"18\">Shortest duration counterexample</text>\n");
        for (int time = 0; time <= maximum; time++) {
            int x = 90 + 660 * time / maximum;
            svg.append(String.format(Locale.ROOT, "<line x1=\"%d\" y1=\"45\" x2=\"%d\" y2=\"220\" stroke=\"#dddddd\"/><text x=\"%d\" y=\"240\" font-family=\"Arial\" font-size=\"16\">%d</text>\n", x, x, x, time));
        }
        for (int i = 0; i < input.size(); i++) {
            int[] job = input.get(i);
            int x = 90 + 660 * job[0] / maximum, width = 660 * (job[1] - job[0]) / maximum, y = 55 + 55 * i;
            String color = greedy.contains(job) ? "#9d3b3b" : optimum.contains(job) ? "#315e85" : "#777777";
            svg.append(String.format(Locale.ROOT, "<text x=\"20\" y=\"%d\" font-family=\"Arial\" font-size=\"16\">J%d</text><rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"30\" fill=\"%s\"/>\n", y + 22, i, x, y, width, color));
        }
        svg.append("<text x=\"90\" y=\"275\" font-family=\"Arial\" font-size=\"16\">Red: greedy selection. Blue: optimum. Touching endpoints are compatible.</text>\n</svg>\n");
        Files.writeString(directory.resolve("counterexample_timeline.svg"), svg.toString(), StandardCharsets.UTF_8);
    }
}
