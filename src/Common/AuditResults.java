package Common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

// Independently verifies the saved experimental evidence without external libraries.
public class AuditResults {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static List<String> fields(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
                else quoted = !quoted;
            } else if (c == ',' && !quoted) { values.add(value.toString()); value.setLength(0); }
            else value.append(c);
        }
        require(!quoted, "Unclosed CSV quote");
        values.add(value.toString());
        return values;
    }

    private static List<Map<String, String>> read(Path directory, String filename) throws IOException {
        List<String> lines = Files.readAllLines(directory.resolve(filename), StandardCharsets.UTF_8);
        require(!lines.isEmpty(), "Empty CSV: " + filename);
        List<String> header = fields(lines.get(0));
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> values = fields(lines.get(i));
            require(values.size() == header.size(), "Wrong CSV column count: " + filename);
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < header.size(); j++) row.put(header.get(j), values.get(j));
            rows.add(row);
        }
        return rows;
    }

    private static List<int[]> jobs(String text) {
        List<int[]> jobs = new ArrayList<>();
        Pattern pattern = Pattern.compile("J(\\d+):\\[(-?\\d+),(-?\\d+)\\)");
        for (String interval : text.split(";")) {
            Matcher match = pattern.matcher(interval);
            require(match.matches(), "Malformed interval: " + interval);
            require(Integer.parseInt(match.group(1)) == jobs.size(), "Nonsequential job ID");
            int start = Integer.parseInt(match.group(2)), finish = Integer.parseInt(match.group(3));
            require(start < finish, "Invalid interval");
            jobs.add(new int[]{start, finish});
        }
        return jobs;
    }

    private static int selection(List<int[]> jobs, String ids) {
        List<int[]> selected = new ArrayList<>();
        if (!ids.isEmpty()) for (String id : ids.split(";")) {
            require(id.matches("J\\d+"), "Malformed selected ID");
            int index = Integer.parseInt(id.substring(1));
            require(index < jobs.size(), "Unknown selected ID");
            selected.add(jobs.get(index));
        }
        ExperimentSupport.validate(jobs, selected);
        return selected.size();
    }

    // Bit-mask enumeration with unsorted pairwise checks, independent of ExactSolver.
    private static int independentOptimum(List<int[]> jobs) {
        require(jobs.size() <= 20, "Audit oracle is only for small inputs");
        int best = 0;
        for (int mask = 0; mask < (1 << jobs.size()); mask++) {
            int size = Integer.bitCount(mask);
            if (size <= best) continue;
            boolean compatible = true;
            for (int i = 0; i < jobs.size() && compatible; i++) {
                if ((mask & (1 << i)) == 0) continue;
                for (int j = 0; j < i; j++) {
                    if ((mask & (1 << j)) == 0) continue;
                    int[] a = jobs.get(i), b = jobs.get(j);
                    if (a[0] < b[1] && b[0] < a[1]) { compatible = false; break; }
                }
            }
            if (compatible) best = size;
        }
        return best;
    }

    private static void close(double actual, String expected) {
        double saved = Double.parseDouble(expected);
        require(Double.isFinite(saved) && Math.abs(actual - saved) <= 1e-9 * Math.max(1, Math.abs(actual)), "Incorrect summary value");
    }

    private static void summary(Map<String, String> summary, List<Map<String, String>> data) {
        String name = summary.get("algorithm");
        int matches = 0;
        double sum = 0, minimum = 1;
        for (Map<String, String> row : data) {
            double ratio = Double.parseDouble(row.get(name + "_ratio"));
            sum += ratio; minimum = Math.min(minimum, ratio);
            if (row.get(name + "_size").equals(row.get("optimum_size"))) matches++;
        }
        require(Integer.parseInt(summary.get("instances")) == data.size(), "Wrong instance total");
        require(Integer.parseInt(summary.get("optimal_matches")) == matches, "Wrong optimal match count");
        close(100.0 * matches / data.size(), summary.get("optimal_percent"));
        close(sum / data.size(), summary.get("average_ratio"));
        if (summary.containsKey("worst_ratio")) close(minimum, summary.get("worst_ratio"));
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) throw new IllegalArgumentException("Use at most one results-directory argument");
        Path directory = Paths.get(args.length == 0 ? "results" : args[0]);
        if (!Files.isDirectory(directory)) throw new IOException("Results missing. Run Common.RunExperiments first.");
        List<Map<String, String>> data = read(directory, "quality_instances.csv");
        require(data.size() == 500, "Expected 500 quality instances");
        Map<String, Map<String, String>> byId = new HashMap<>();
        for (Map<String, String> row : data) {
            require(byId.put(row.get("instance_id"), row) == null, "Repeated instance ID");
            List<int[]> input = jobs(row.get("intervals"));
            require(input.size() == Integer.parseInt(row.get("n")), "Wrong job count");
            int optimum = independentOptimum(input);
            require(optimum == Integer.parseInt(row.get("optimum_size")), "Incorrect saved optimum");
            require(selection(input, row.get("optimal_ids")) == optimum, "Incorrect optimum schedule");
            for (int a = 0; a < 4; a++) {
                String name = ExperimentSupport.NAMES[a];
                int size = selection(input, row.get(name + "_ids"));
                require(size == Integer.parseInt(row.get(name + "_size")), "Incorrect saved size");
                require(ExperimentSupport.ids(input, ExperimentSupport.ALGORITHMS.get(a).apply(input)).equals(row.get(name + "_ids")), "Saved heuristic differs from implementation");
                close((double) size / optimum, row.get(name + "_ratio"));
            }
        }
        for (Map<String, String> row : read(directory, "quality_summary.csv")) summary(row, data);
        for (Map<String, String> row : read(directory, "quality_by_distribution.csv")) {
            List<Map<String, String>> subset = new ArrayList<>();
            for (Map<String, String> instance : data) if (instance.get("distribution").equals(row.get("distribution"))) subset.add(instance);
            require(subset.size() == 125, "Wrong distribution total");
            summary(row, subset);
        }
        for (Map<String, String> row : read(directory, "worst_instances.csv")) {
            Map<String, String> original = byId.get(row.get("instance_id"));
            require(original != null && row.get("intervals").equals(original.get("intervals")), "Wrong worst-instance input");
            require(row.get("selected_ids").equals(original.get(row.get("algorithm") + "_ids")), "Wrong worst-instance selection");
            require(row.get("optimal_ids").equals(original.get("optimal_ids")), "Wrong worst-instance optimum");
            close(Double.parseDouble(original.get(row.get("algorithm") + "_ratio")), row.get("ratio"));
        }
        List<Map<String, String>> examples = read(directory, "counterexamples.csv");
        require(examples.size() == 2, "Expected two searched failures");
        for (Map<String, String> row : examples) {
            List<int[]> input = jobs(row.get("intervals"));
            int optimum = independentOptimum(input), size = selection(input, row.get("selected_ids"));
            require(size == Integer.parseInt(row.get("selected_size")) && size < optimum, "Invalid counterexample");
            require(selection(input, row.get("optimal_ids")) == optimum && optimum == Integer.parseInt(row.get("optimum_size")), "Invalid counterexample optimum");
        }
        for (Map<String, String> row : read(directory, "search_summary.csv")) {
            require(Integer.parseInt(row.get("instances_checked")) == 7546, "Wrong search count");
            boolean fails = row.get("algorithm").equals("EarliestStart") || row.get("algorithm").equals("ShortestDuration");
            require(Boolean.parseBoolean(row.get("counterexample_found")) == fails, "Wrong search outcome");
        }
        List<Map<String, String>> searched = read(directory, "search_instances.csv");
        require(searched.size() == 7546, "Expected complete evidence for every search input");
        for (Map<String, String> row : searched) {
            List<int[]> input = jobs(row.get("intervals"));
            int optimum = independentOptimum(input);
            require(optimum == Integer.parseInt(row.get("optimum_size")), "Wrong search optimum");
            require(selection(input, row.get("optimal_ids")) == optimum, "Wrong search optimum selection");
            for (int a = 0; a < 4; a++) {
                String name = ExperimentSupport.NAMES[a];
                int size = selection(input, row.get(name + "_ids"));
                require(size <= optimum && size == Integer.parseInt(row.get(name + "_size")), "Wrong search selection size");
                require(ExperimentSupport.ids(input, ExperimentSupport.ALGORITHMS.get(a).apply(input)).equals(row.get(name + "_ids")), "Wrong search heuristic selection");
                if (a == 0 || a == 3) require(size == optimum, "Optimal greedy failed search");
            }
        }
        List<Map<String, String>> tests = read(directory, "test_results.csv");
        require(tests.size() == 2, "Expected evidence for both Java test suites");
        for (Map<String, String> row : tests) require(row.get("status").equals("passed"), "Saved test suite did not pass");
        List<Map<String, String>> samples = read(directory, "runtime_samples.csv");
        require(samples.size() == 112, "Expected 112 runtime samples");
        for (Map<String, String> row : read(directory, "runtime_summary.csv")) {
            List<Long> times = new ArrayList<>();
            Set<String> repeats = new HashSet<>();
            for (Map<String, String> sample : samples) {
                if (!sample.get("algorithm").equals(row.get("algorithm")) || !sample.get("n").equals(row.get("n"))) continue;
                require(repeats.add(sample.get("repeat")), "Repeated benchmark repetition");
                long elapsed = Long.parseLong(sample.get("elapsed_ns"));
                require(elapsed > 0, "Nonpositive runtime"); times.add(elapsed);
            }
            require(times.size() == 7 && row.get("repeats").equals("7"), "Wrong repetition count");
            Collections.sort(times);
            close(times.get(3) / 1e6, row.get("median_ms"));
            close(times.get(0) / 1e6, row.get("min_ms"));
            close(times.get(6) / 1e6, row.get("max_ms"));
        }
        System.out.println("Audited 500 quality and 7546 search optimums, saved selections, summaries, and 112 timing samples.");
    }
}
