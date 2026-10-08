import java.util.*;

public class IntervalScheduling {
    public static List<int[]> earliestFinishTime(List<int[]> jobs) {
        List<int[]> sortedJobs = new ArrayList<>(jobs);

        //sort jobs bt finish time, then start time
        sortedJobs.sort(
            Comparator.comparingInt((int[] job) -> job[1])
                      .thenComparingInt(job -> job[0])

        );

        List<int[]> selectedJobs = new ArrayList<>();
        long lastFinish = Long.MIN_VALUE;

        for(int[] job : sortedJobs) {
            if(job[0] >= lastFinish) {
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

        List<int[]> result = earliestFinishTime(jobs);

        System.out.println("Selected jobs:");

        for(int[] job : result) {
            System.out.println("(" + job[0] + ", " + job[1] + ")");
        }

        System.out.println("Number of jobs selected: " + result.size());
    }
}