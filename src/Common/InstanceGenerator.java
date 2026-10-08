import java.util.*;

public final class InstanceGenerator {
    public static final String[] TYPES = {"sparse", "overlapping", "varied", "clustered"};
    private InstanceGenerator() {}

    public static List<int[]> generate(String type, int n, Random random) {
        List<int[]> jobs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int start, duration;
            switch (type) {
                case "sparse":
                    start = random.nextInt(100); duration = 1 + random.nextInt(4); break;
                case "overlapping":
                    start = random.nextInt(11); duration = 30 + random.nextInt(21) - start; break;
                case "varied":
                    start = random.nextInt(41); duration = 1 + random.nextInt(30); break;
                case "clustered":
                    start = 10 * random.nextInt(3) + random.nextInt(4); duration = 1 + random.nextInt(12); break;
                default: throw new IllegalArgumentException("Unknown distribution: " + type);
            }
            jobs.add(new int[]{start, start + duration});
        }
        return jobs;
    }

    public static List<int[]> benchmark(int n, long seed) {
        Random random = new Random(seed);
        List<int[]> jobs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int start = random.nextInt(10 * n);
            jobs.add(new int[]{start, start + 1 + random.nextInt(100)});
        }
        return jobs;
    }
}
