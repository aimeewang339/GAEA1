package Common;

import P1.*;
import P2.ExactSolver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public final class ExperimentSupport {
    public static final String[] NAMES = {"EarliestFinish", "EarliestStart", "ShortestDuration", "LatestStart"};
    public static final List<Function<List<int[]>, List<int[]>>> ALGORITHMS = Arrays.asList(
        EarliestFinishTime::earliestFinishTime, EarliestStartTime::earliestStartTime,
        ShortestDuration::shortestDuration, LatestStartTime::latestStartTime);

    private ExperimentSupport() {}

    public static Path output(String[] args) throws IOException {
        if (args.length > 1) throw new IllegalArgumentException("Use at most one output-directory argument");
        Path path = Paths.get(args.length == 0 ? "results" : args[0]);
        Files.createDirectories(path);
        return path;
    }

    public static PrintWriter csv(Path directory, String filename) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(directory.resolve(filename), StandardCharsets.UTF_8));
    }

    public static void row(PrintWriter writer, Object... values) {
        StringJoiner row = new StringJoiner(",");
        for (Object value : values) row.add("\"" + value.toString().replace("\"", "\"\"") + "\"");
        writer.println(row);
        if (writer.checkError()) throw new UncheckedIOException(new IOException("Failed to write results"));
    }

    public static String intervals(List<int[]> jobs) {
        StringJoiner text = new StringJoiner(";");
        for (int i = 0; i < jobs.size(); i++) {
            int[] job = jobs.get(i);
            text.add("J" + i + ":[" + job[0] + "," + job[1] + ")");
        }
        return text.toString();
    }

    public static String ids(List<int[]> input, List<int[]> selected) {
        IdentityHashMap<int[], Integer> ids = new IdentityHashMap<>();
        for (int i = 0; i < input.size(); i++) ids.put(input.get(i), i);
        StringJoiner result = new StringJoiner(";");
        for (int[] job : selected) {
            Integer id = ids.get(job);
            if (id == null) throw new AssertionError("Selected unknown job");
            result.add("J" + id);
        }
        return result.toString();
    }

    public static void validate(List<int[]> input, List<int[]> selected) {
        // Chronological check is O(k log k), including for large benchmarks.
        Set<int[]> available = Collections.newSetFromMap(new IdentityHashMap<>());
        available.addAll(input);
        for (int[] job : selected) {
            if (!available.remove(job)) throw new AssertionError("Unknown or repeated job");
        }
        List<int[]> sorted = new ArrayList<>(selected);
        sorted.sort(Comparator.comparingInt((int[] job) -> job[0]));
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i - 1)[1] > sorted.get(i)[0]) throw new AssertionError("Overlapping output");
        }
    }

    public static void sanity() {
        List<int[]> start = Arrays.asList(new int[]{0,10}, new int[]{1,2}, new int[]{2,3}, new int[]{3,4}, new int[]{4,5});
        List<int[]> duration = Arrays.asList(new int[]{2,4}, new int[]{0,3}, new int[]{3,6});
        if (EarliestStartTime.earliestStartTime(start).size() != 1 || ExactSolver.exactSolver(start).size() != 4
            || ShortestDuration.shortestDuration(duration).size() != 1 || ExactSolver.exactSolver(duration).size() != 2) {
            throw new AssertionError("Assignment sanity failure");
        }
    }
}
