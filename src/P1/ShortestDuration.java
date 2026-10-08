import java.util.*;

public class ShortestDuration {
    // O(n^2) time, O(n) extra space. Identical jobs retain input order.
    public static List<int[]> shortestDuration(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);
        sortedJobs.sort(Comparator.comparingLong((int[] job) -> (long) job[1] - job[0])
            .thenComparingInt(job -> job[0]).thenComparingInt(job -> job[1]));
        List<int[]> selectedJobs = new ArrayList<>();
        for (int[] job : sortedJobs) {
            boolean compatible = true;
            // Duration order is not chronological: check all selected jobs.
            for (int[] selected : selectedJobs) {
                if (job[0] < selected[1] && selected[0] < job[1]) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) selectedJobs.add(job);
        }
        return selectedJobs;
    }

    public static void main(String[] args) {
        List<int[]> result = shortestDuration(Arrays.asList(
            new int[]{2, 4}, new int[]{0, 3}, new int[]{3, 6}));
        System.out.println("Selected jobs:");
        for (int[] job : result) {
            System.out.println("(" + job[0] + ", " + job[1] + ")");
        }
        System.out.println("Number of jobs selected: " + result.size());
    }
}
