import java.util.*;

public class ShortestDuration {
    // O(n log n) time, O(n) extra space. Identical jobs retain input order.
    public static List<int[]> shortestDuration(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);
        sortedJobs.sort(Comparator.comparingLong((int[] job) -> (long) job[1] - job[0])
            .thenComparingInt(job -> job[0]).thenComparingInt(job -> job[1]));
        List<int[]> selectedJobs = new ArrayList<>();
        TreeMap<Integer, int[]> selectedByStart = new TreeMap<>();
        for (int[] job : sortedJobs) {
            // Selected jobs are disjoint. Only the immediate neighbors can overlap.
            Map.Entry<Integer, int[]> before = selectedByStart.floorEntry(job[0]);
            Map.Entry<Integer, int[]> after = selectedByStart.ceilingEntry(job[0]);
            if (before != null && before.getValue()[1] > job[0]) continue;
            if (after != null && job[1] > after.getKey()) continue;
            selectedJobs.add(job);
            selectedByStart.put(job[0], job);
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
