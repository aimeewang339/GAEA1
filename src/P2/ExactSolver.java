import java.util.*;

public class ExactSolver {
    // Exhaustive include/skip search, not a greedy selection rule.
    // O(2^n) worst-case time and O(n) auxiliary space; intended for small inputs.
    public static List<int[]> exactSolver(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);
        sortedJobs.sort(Comparator.comparingInt((int[] job) -> job[0])
            .thenComparingInt(job -> job[1]));
        List<int[]> best = new ArrayList<>();
        search(sortedJobs, 0, Long.MIN_VALUE, new ArrayList<>(), best);
        return best;
    }

    private static void search(List<int[]> jobs, int index, long lastFinish,
                               List<int[]> current, List<int[]> best) {
        if (index == jobs.size()) {
            // Keep the first maximum found for deterministic tie-breaking.
            if (current.size() > best.size()) {
                best.clear();
                best.addAll(current);
            }
            return;
        }

        int[] job = jobs.get(index);
        // Sorted start times make checking the last selected finish sufficient.
        if (job[0] >= lastFinish) {
            current.add(job);
            search(jobs, index + 1, job[1], current, best);
            current.remove(current.size() - 1);
        }
        // Always explore skipping, even if the job was compatible.
        search(jobs, index + 1, lastFinish, current, best);
    }

    public static void main(String[] args) {
        List<int[]> jobs = Arrays.asList(
            new int[]{0, 10}, new int[]{1, 2}, new int[]{2, 3},
            new int[]{3, 4}, new int[]{4, 5});
        List<int[]> result = exactSolver(jobs);
        System.out.println("Optimal jobs:");
        for (int[] job : result) {
            System.out.println("(" + job[0] + ", " + job[1] + ")");
        }
        System.out.println("Number of jobs selected: " + result.size());
    }
}
