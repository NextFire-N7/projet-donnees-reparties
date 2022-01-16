package linda.primes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.shm.CentralizedLinda;

/**
 * Dumb attempt to parallelize a dumb algorithm.
 */
public class Parallel implements Callable<Collection<Integer>> {

    /**
     * Thread runnable.
     */
    private class Worker implements Runnable {

        /** The potential prime to consider */
        private int start;
        /** true: kill worker */
        private boolean abort;

        public Worker(int start) {
            this.start = start;
            abort = false;
        }

        @Override
        public void run() {
            // (start, false) present == not prime, abort
            linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, new Tuple(start, false), t -> {
                abort = true;
            });

            // mark all multiples of start as not prime
            for (int i = start + start; i <= end; i += start) {
                if (abort) {
                    return;
                }

                // remove multiple from the sieve
                linda.tryTake(new Tuple(i, true));
                // mark it as not prime
                linda.write(new Tuple(i, false));
            }
        }

    }

    private int end;
    private Linda linda;
    private ExecutorService executor;

    public Parallel(int end) {
        this.end = end;
        linda = new CentralizedLinda();
        executor = Executors.newWorkStealingPool();
    }

    @Override
    public Collection<Integer> call() throws Exception {
        // mark all numbers as prime
        for (int i = 2; i <= end; i++) {
            linda.write(new Tuple(i, true));
        }

        // feed executor with workers
        for (int i = 2; i <= end; i++) {
            executor.execute(new Worker(i));
        }

        // wait for workers to finish
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        // collect primes
        Collection<Integer> primes = new ArrayList<>();
        for (Tuple t : linda.readAll(new Tuple(Integer.class, true))) {
            primes.add((int) t.get(0));
        }

        return primes;
    }

}
