import java.util.*;

public class ExactSolverTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean compatible(List<int[]> jobs) {
        for (int i = 0; i < jobs.size(); i++) {
            for (int j = 0; j < i; j++) {
                int[] a = jobs.get(i), b = jobs.get(j);
                if (a[0] < b[1] && b[0] < a[1]) return false;
            }
        }
        return true;
    }

    // Independent oracle: enumerate all masks and check every pair, without sorting.
    private static int subsetOptimum(List<int[]> jobs) {
        int best = 0;
        for (int mask = 0; mask < (1 << jobs.size()); mask++) {
            List<int[]> subset = new ArrayList<>();
            for (int i = 0; i < jobs.size(); i++) {
                if ((mask & (1 << i)) != 0) subset.add(jobs.get(i));
            }
            if (compatible(subset)) best = Math.max(best, subset.size());
        }
        return best;
    }

    private static void verify(List<int[]> jobs, int expected) {
        List<int[]> original = new ArrayList<>(jobs);
        List<int[]> result = ExactSolver.exactSolver(jobs);
        require(result.size() == expected, "Incorrect optimum size");
        require(compatible(result), "Selected overlapping jobs");
        List<int[]> remaining = new ArrayList<>(jobs);
        for (int[] job : result) {
            require(remaining.remove(job), "Unknown or repeated input job");
        }
        require(jobs.equals(original), "Changed input order");
        require(result.equals(ExactSolver.exactSolver(jobs)), "Nondeterministic result");
    }

    public static void main(String[] args) {
        verify(Collections.emptyList(), 0);
        verify(Arrays.asList(new int[]{5, 8}), 1);
        verify(Arrays.asList(new int[]{0, 10}, new int[]{1, 2},
            new int[]{2, 3}, new int[]{3, 4}, new int[]{4, 5}), 4);
        verify(Arrays.asList(new int[]{2, 4}, new int[]{0, 3}, new int[]{3, 6}), 2);
        verify(Arrays.asList(new int[]{2, 3}, new int[]{0, 1}, new int[]{1, 2}), 3);
        verify(Arrays.asList(new int[]{0, 5}, new int[]{1, 4}, new int[]{2, 3}), 1);
        verify(Arrays.asList(new int[]{0, 1}, new int[]{0, 1}), 1);
        verify(Arrays.asList(new int[]{0, 10}, new int[]{0, 2}, new int[]{2, 3}), 2);
        verify(Arrays.asList(new int[]{Integer.MIN_VALUE, -1},
            new int[]{-1, 0}, new int[]{0, Integer.MAX_VALUE}), 3);

        Random random = new Random(4050);
        for (int trial = 0; trial < 300; trial++) {
            List<int[]> jobs = new ArrayList<>();
            for (int i = 0, n = random.nextInt(11); i < n; i++) {
                int start = random.nextInt(21) - 10;
                jobs.add(new int[]{start, start + 1 + random.nextInt(10)});
            }
            verify(jobs, subsetOptimum(jobs));
        }
        System.out.println("Passed 9 fixed cases and 300 seeded subset-oracle comparisons.");
    }
}
