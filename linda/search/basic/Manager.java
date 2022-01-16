package linda.search.basic;

import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;
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

    public Manager(Linda linda, String pathname, String search) {
        this.linda = linda;
        this.pathname = pathname;
        this.search = search;
        this.reqUUID = UUID.randomUUID();
    }

    private void addSearch(String search) {
        this.search = search;
        System.out.println("Search " + this.reqUUID + " for " + this.search);
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

    // synchronized ne pose ici aucun problème : le wait est souvent appelé et relache le verrou.
    // La partie critique est très courte en temps d'exécution.
    synchronized private void waitForEndSearch() {
        //Wait for a searcher to pick up the request
        linda.take(new Tuple(Code.Searcher, "searching", this.reqUUID));

        //Then wait until no workers are still affected to the task (either done or interrupted).
        do {
            try {
                //wait for new results
                this.wait(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (!linda.takeAll(new Tuple(Code.Searcher, "searching", this.reqUUID)).isEmpty());

        linda.take(new Tuple(Code.Request, this.reqUUID, String.class, Integer.class)); // remove query
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
            linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);
        }
    }

    public void run() {
        this.loadData(pathname);
        this.addSearch(search);
        this.waitForEndSearch();
    }
}
