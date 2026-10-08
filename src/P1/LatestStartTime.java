package P1;

import java.util.*;

public class LatestStartTime {
    // Schedule backward. O(n log n) time, O(n) extra space.
    public static List<int[]> latestStartTime(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);
        // Latest start, then latest finish; identical jobs retain input order.
        sortedJobs.sort(Comparator.comparingInt((int[] job) -> job[0]).reversed()
            .thenComparing(Comparator.comparingInt((int[] job) -> job[1]).reversed()));
        List<int[]> selectedJobs = new ArrayList<>();
        long nextStart = Long.MAX_VALUE;
        for (int[] job : sortedJobs) {
            // Half-open intervals allow touching endpoints.
            if (job[1] <= nextStart) {
                selectedJobs.add(job);
                nextStart = job[0];
            }
        }
        Collections.reverse(selectedJobs);
        return selectedJobs;
    }

    public static void main(String[] args) {
        List<int[]> result = latestStartTime(Arrays.asList(
            new int[]{0, 10}, new int[]{1, 2}, new int[]{2, 3},
            new int[]{3, 4}, new int[]{4, 5}));
        System.out.println("Selected jobs:");
        for (int[] job : result) {
            System.out.println("(" + job[0] + ", " + job[1] + ")");
        }
        System.out.println("Number of jobs selected: " + result.size());
    }
}
