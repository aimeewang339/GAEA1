import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RuntimeExperiment {
    private static volatile int consumed;
    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final int REPEATS = 7;
    private static final long SEED = 60602026L;

    public static void main(String[] args) throws IOException {
        Path directory = ExperimentSupport.output(args);
        try (PrintWriter raw = ExperimentSupport.csv(directory, "runtime_samples.csv");
             PrintWriter summary = ExperimentSupport.csv(directory, "runtime_summary.csv")) {
            ExperimentSupport.row(raw, "algorithm", "n", "seed", "repeat", "elapsed_ns", "selected_size");
            ExperimentSupport.row(summary, "algorithm", "n", "repeats", "median_ms", "min_ms", "max_ms");
            // Warm up each algorithm before measurements; no exact solver in this benchmark.
            List<int[]> warmup = InstanceGenerator.benchmark(10000, SEED);
            for (int pass = 0; pass < 5; pass++) {
                for (int a = 0; a < 4; a++) consumed = ExperimentSupport.ALGORITHMS.get(a).apply(warmup).size();
            }
            for (int n : SIZES) {
                List<int[]> input = InstanceGenerator.benchmark(n, SEED + n);
                // Warm this size to reduce initial allocation and compilation effects.
                for (int a = 0; a < 4; a++) {
                    for (int pass = 0; pass < 2; pass++) consumed = ExperimentSupport.ALGORITHMS.get(a).apply(input).size();
                }
                long[][] samples = new long[4][REPEATS];
                for (int repeat = 0; repeat < REPEATS; repeat++) {
                    // Rotate execution order to avoid always placing one algorithm first.
                    for (int offset = 0; offset < 4; offset++) {
                        int a = (repeat + offset) % 4;
                        long begin = System.nanoTime();
                        List<int[]> selected = ExperimentSupport.ALGORITHMS.get(a).apply(input);
                        long elapsed = System.nanoTime() - begin;
                        consumed = selected.size();
                        ExperimentSupport.validate(input, selected); // Outside the measured region.
                        samples[a][repeat] = elapsed;
                        ExperimentSupport.row(raw, ExperimentSupport.NAMES[a], n, SEED + n, repeat, elapsed, selected.size());
                    }
                }
                for (int a = 0; a < 4; a++) {
                    Arrays.sort(samples[a]);
                    ExperimentSupport.row(summary, ExperimentSupport.NAMES[a], n, REPEATS,
                        samples[a][REPEATS / 2] / 1e6, samples[a][0] / 1e6, samples[a][REPEATS - 1] / 1e6);
                }
                System.out.println("Benchmarked all four algorithms at n=" + n);
            }
        }
        try (PrintWriter environment = ExperimentSupport.csv(directory, "runtime_environment.csv")) {
            ExperimentSupport.row(environment, "property", "value");
            for (String key : Arrays.asList("java.version", "java.vm.name", "os.name", "os.version", "os.arch")) {
                ExperimentSupport.row(environment, key, System.getProperty(key));
            }
            ExperimentSupport.row(environment, "available_processors", Runtime.getRuntime().availableProcessors());
            ExperimentSupport.row(environment, "processor", System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "unknown"));
            ExperimentSupport.row(environment, "seed_base", SEED);
            ExperimentSupport.row(environment, "generator", "start uniform [0,10*n); duration uniform [1,100]");
            ExperimentSupport.row(environment, "warmup", "5 passes at n=10000 plus 2 passes per algorithm per measured size");
        }
    }
}
