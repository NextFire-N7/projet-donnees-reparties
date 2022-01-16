package linda.primes;

import java.util.Collection;

public class Benchmark {

    public static void main(String[] args) throws Exception {
        // 1229 primes entre 0 et 10000

        int end = Integer.parseInt(args[0]);

        Collection<Integer> primes = null;
        long startTime, endTime;

        for (int i = 0; i < 5; i++) {
            startTime = System.currentTimeMillis();
            primes = new Sequential(end).call();
            endTime = System.currentTimeMillis();
            System.out.println("Sequential: " + (endTime - startTime) + " ms");
        }

        System.out.println(primes.size());

        for (int i = 0; i < 5; i++) {
            startTime = System.currentTimeMillis();
            primes = new Parallel(end).call();
            endTime = System.currentTimeMillis();
            System.out.println("Parallel: " + (endTime - startTime) + " ms");
        }

        System.out.println(primes.size());
    }

}
