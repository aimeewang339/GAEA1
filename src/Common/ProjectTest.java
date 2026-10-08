package Common;

import P1.ShortestDuration;
import P2.ExactSolver;
import java.util.*;

public class ProjectTest {
    private static List<int[]> referenceDuration(List<int[]> input) {
        List<int[]> sorted = new ArrayList<>(input), selected = new ArrayList<>();
        sorted.sort(Comparator.comparingLong((int[] job) -> (long) job[1] - job[0])
            .thenComparingInt(job -> job[0]).thenComparingInt(job -> job[1]));
        for (int[] job : sorted) {
            boolean compatible = true;
            for (int[] other : selected) if (job[0] < other[1] && other[0] < job[1]) { compatible = false; break; }
            if (compatible) selected.add(job);
        }
        return selected;
    }

    private static void verify(List<int[]> input) {
        List<int[]> original = new ArrayList<>(input);
        if (!referenceDuration(input).equals(ShortestDuration.shortestDuration(input))) {
            throw new AssertionError("Ordered-neighbor check changed duration selections");
        }
        int optimum = ExactSolver.exactSolver(input).size();
        for (int a = 0; a < 4; a++) {
            List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
            ExperimentSupport.validate(input, selected);
            if (selected.size() > optimum || ((a == 0 || a == 3) && selected.size() != optimum)) {
                throw new AssertionError("Optimality disagreement");
            }
            if (!selected.equals(ExperimentSupport.ALGORITHMS.get(a).apply(input))) throw new AssertionError("Nondeterministic output");
        }
        if (!input.equals(original)) throw new AssertionError("Input changed");
    }

    public static void main(String[] args) {
        ExperimentSupport.sanity();
        verify(Collections.emptyList());
        verify(Arrays.asList(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}, new int[]{0,1}));
        verify(Arrays.asList(new int[]{5,6}, new int[]{0,5}, new int[]{6,9}));
        verify(Arrays.asList(new int[]{0,1}, new int[]{0,1}, new int[]{1,2}));
        Random random = new Random(1234050);
        for (int trial = 0; trial < 1000; trial++) {
            String type = InstanceGenerator.TYPES[trial % 4];
            verify(InstanceGenerator.generate(type, random.nextInt(13), random));
        }
        System.out.println("Passed project sanity, boundary, determinism, and 1000 seeded duration-equivalence checks.");
    }
}
