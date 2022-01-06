package linda.primes;

import java.util.ArrayList;
import java.util.List;

public class Sequential {

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        boolean[] isNotPrime = new boolean[n + 1];
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            if (!isNotPrime[i]) {
                primes.add(i);
                for (int j = i * i; j <= n; j += i) {
                    isNotPrime[j] = true;
                }
            }
        }
    }

}
