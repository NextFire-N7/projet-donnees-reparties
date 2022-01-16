package linda.primes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

/**
 * Classic and dumb sequential algorithm.
 */
public class Sequential implements Callable<Collection<Integer>> {

    private int start;
    private int end;
    private Linda linda;

    public Sequential(int start, int end) {
        this.start = start;
        this.end = end;
        linda = new CentralizedLinda();
    }

    public Sequential(int end) {
        this(2, end);
    }

    @Override
    public Collection<Integer> call() {
        Collection<Integer> primes = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (linda.tryRead(new Tuple(i)) == null) {
                primes.add(i);
                for (int j = i + i; j <= end; j += i) {
                    linda.write(new Tuple(j));
                }
            }
        }
        return primes;
    }

}
