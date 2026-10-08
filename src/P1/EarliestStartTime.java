package P1;

import java.util.*;

public class EarliestStartTime {
    // Order by start time, then finish time. Identical intervals keep input order.
    // Sorting takes O(n log n); the scan takes O(n), with O(n) extra space.
    public static List<int[]> earliestStartTime(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);

        sortedJobs.sort(
            Comparator.comparingInt((int[] job) -> job[0])
                      .thenComparingInt(job -> job[1])
        );

        List<int[]> selectedJobs = new ArrayList<>();
        long lastFinish = Long.MIN_VALUE;

        for (int[] job : sortedJobs) {
            // Half-open intervals allow a job to start when the previous one ends.
            if (job[0] >= lastFinish) {
                selectedJobs.add(job);
                lastFinish = job[1];
            }
        }

        return selectedJobs;
    }

    public static void main(String[] args) {
        List<int[]> jobs = Arrays.asList(
            new int[]{0, 10},
            new int[]{1, 2},
            new int[]{2, 3},
            new int[]{3, 4},
            new int[]{4, 5}
        );

        List<int[]> result = earliestStartTime(jobs);

        System.out.println("Selected jobs:");
        for (int[] job : result) {
            System.out.println("(" + job[0] + ", " + job[1] + ")");
        }
        System.out.println("Number of jobs selected: " + result.size());
    }
}
