package linda.primes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

public class Parallel implements Callable<Collection<Integer>> {

    private class Worker implements Runnable {

        private int start;

        public Worker(int start) {
            this.start = start;
        }

        @Override
        public void run() {
            for (int i = start; i <= end; i++) {
                if (linda.tryRead(new Tuple(i)) != null) {
                    for (int j = i + i; j <= end; j += i) {
                        linda.tryTake(new Tuple(j));
                    }
                }
            }
        }

    }

    private int end;
    private Linda linda;
    private ExecutorService executor;
    private Collection<Integer> primes;

    public Parallel(int end) {
        this.end = end;
        linda = new CentralizedLinda();
        executor = Executors.newWorkStealingPool();
        primes = new ArrayList<>();
    }

    @Override
    public Collection<Integer> call() {
        for (int i = 2; i <= end; i++) {
            linda.write(new Tuple(i));
        }

        for (int i = 2; i <= end; i += 100) {
            executor.execute(new Worker(i));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Tuple t : linda.readAll(new Tuple(Integer.class))) {
            primes.add((int) t.get(0));
        }

        return primes;
    }

}
