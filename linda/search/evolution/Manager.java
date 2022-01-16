package linda.search.evolution;

import java.util.UUID;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;

public class Manager implements Runnable {

    private Linda linda;

    private UUID reqUUID;
    private String pathname;
    private String search;
    private int bestvalue = Integer.MAX_VALUE; // lower is better
    private String bestresult;

    private int max_time = 10;
    private boolean has_finished = false;

    public Manager(Linda linda, String pathname, String search) {
        this.linda = linda;
        this.pathname = pathname;
        this.search = search;
        this.reqUUID = UUID.randomUUID();
    }

    public Manager(Linda linda, String pathname, String search, int max_time_millis) {
        this(linda, pathname, search);
        this.max_time = max_time_millis;
    }

    private void addSearch(String search) {
        this.search = search;
        System.out.println("Search " + this.reqUUID + " for " + this.search);
        linda.write(new Tuple(Code.Manager, this.reqUUID));
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, this.reqUUID, String.class, Integer.class), new CbGetResult());
        linda.write(new Tuple(Code.Request, this.reqUUID, this.search, 0));
    }

    private void loadData(String pathname) {
        try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
            stream.limit(10000).forEach(s -> linda.write(new Tuple(Code.Value, this.reqUUID, s.trim())));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForEndSearch() {
        // Search time-out example
        Object mutex = new Object();
        Thread t = new Thread() {
            public void run() {
                //Wait for a searcher to pick up the request
                linda.take(new Tuple(Code.Searcher, "searching", reqUUID));

                //Then wait until no workers are still affected to the task (either done or interrupted).
                do {
                    try {
                        //wait for new results
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (!linda.takeAll(new Tuple(Code.Searcher, "searching", reqUUID)).isEmpty());

                // Seach has ended => Notify to continue before the timeout.
                synchronized(mutex){
                    mutex.notify();
                }
        }};
        t.start();
        try {
            synchronized(mutex){
                // Wait on timeout or end of search
                if (this.max_time > 0)
                    mutex.wait(max_time);
            }
        } catch (InterruptedException err) {
            throw new RuntimeException(err);
        }

        has_finished = true;
        // signal end of search
        linda.take(new Tuple(Code.Manager, reqUUID));
        linda.tryTake(new Tuple(Code.Request, reqUUID, String.class, Integer.class)); // remove query
        System.out.println("query done");
    }

    private class CbGetResult implements linda.Callback {
        public void call(Tuple t) {  // [ Result, ?UUID, ?String, ?Integer ]
            String s = (String) t.get(2);
            Integer v = (Integer) t.get(3);
            if (v < bestvalue) {
                bestvalue = v;
                bestresult = s;
                System.out.println("New best (" + bestvalue + "): \"" + bestresult + "\"");
            }
            // No more events if the search has ended.
            if (!has_finished)
                linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);
        }
    }

    public void run() {
        this.loadData(pathname);
        this.addSearch(search);
        this.waitForEndSearch();
    }
}
