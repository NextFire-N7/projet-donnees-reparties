package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import linda.AsynchronousCallback;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    /**
     * Représente un "match" en attente avec son template et callback
     */
    private class Event {

        private Tuple template;
        private Callback callback;

        public Event(Tuple template, Callback callback) {
            this.template = template;
            this.callback = callback;
        }

        /**
         * Teste si un tuple match le template de l'event
         * 
         * @param tuple à tester
         * @return le résultat du match du tuple en entrée avec le template de l'event
         */
        public boolean testMatch(Tuple tuple) {
            return tuple.matches(template);
        }

        /**
         * Répond au callback de l'event
         * 
         * @param tuple à passer au callback
         */
        public void resolve(Tuple tuple) {
            callback.call(tuple);
        }

        @Override
        public String toString() {
            return template.toString();
        }

    }

    /** Espace des tuples */
    private List<Tuple> tupleSpace;
    /** Takes en attente */
    private List<Event> takeEvents;
    /** Reads en attente */
    private List<Event> readEvents;

    public CentralizedLinda() {
        // On utilise une CopyOnWriteArrayList aka "A thread-safe variant of ArrayList"
        tupleSpace = new CopyOnWriteArrayList<>();
        takeEvents = new CopyOnWriteArrayList<>();
        readEvents = new CopyOnWriteArrayList<>();
    }

    @Override
    public void write(Tuple t) {
        // Check des read en attente
        for (Event rEvent : readEvents) {
            if (rEvent.testMatch(t)) {
                readEvents.remove(rEvent);
                rEvent.resolve(t);
            }
        }
        // Check des takes en attente
        for (Event tEvent : takeEvents) {
            if (tEvent.testMatch(t)) {
                takeEvents.remove(tEvent);
                tEvent.resolve(t);
                // On ne sauvegarde pas le tuple si on a trouvé un take correspondant
                // (le plus vieux)
                return;
            }
        }
        tupleSpace.add(t);
    }

    @Override
    public Tuple take(Tuple template) {
        return takeRead(template, eventMode.TAKE);
    }

    @Override
    public Tuple read(Tuple template) {
        return takeRead(template, eventMode.READ);
    }

    private Tuple takeRead(Tuple template, eventMode mode) {
        // Queue bloquante
        BlockingQueue<Tuple> queue = new LinkedBlockingQueue<>();
        try {
            // On passe par le systeme d'event (immédiat) qui permet le blocage si on en a
            // pas trouvé dans le space actuel
            eventRegister(mode, eventTiming.IMMEDIATE, template, queue::offer);
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        return tryTakeRead(template, eventMode.TAKE);
    }

    @Override
    public Tuple tryRead(Tuple template) {
        return tryTakeRead(template, eventMode.READ);
    }

    private Tuple tryTakeRead(Tuple template, eventMode mode) {
        // Itération simple sur le space (méthode non bloquante)
        for (Tuple tuple : tupleSpace) {
            if (tuple.matches(template)) {
                if (mode == eventMode.TAKE) {
                    tupleSpace.remove(tuple);
                }
                // On s'arrete sur le premier (le plus vieux)
                return tuple;
            }
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return takeReadAll(template, eventMode.TAKE);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return takeReadAll(template, eventMode.READ);
    }

    private Collection<Tuple> takeReadAll(Tuple template, eventMode mode) {
        // Itération simple sur le space (méthode non bloquante)
        List<Tuple> matched = new ArrayList<>();
        for (Tuple tuple : tupleSpace) {
            if (tuple.matches(template)) {
                if (mode == eventMode.TAKE) {
                    tupleSpace.remove(tuple);
                }
                matched.add(tuple);
            }
        }
        return matched;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        Callback asyncCallback = new AsynchronousCallback(callback);

        if (timing == eventTiming.IMMEDIATE) {
            // Si le timing est immédiat, on itère sur le space actuel et répond direct au
            // callback si on a trouvé un tuple correspondant
            Tuple match = tryTakeRead(template, mode);
            if (match != null) {
                asyncCallback.call(match);
                return;
            }
        }
        // Sinon on enregistre l'event en queue de la liste appropriée
        Event event = new Event(template, asyncCallback);
        if (mode == eventMode.READ) {
            readEvents.add(event);
        } else if (mode == eventMode.TAKE) {
            takeEvents.add(event);
        }
    }

    @Override
    public void debug(String prefix) {
        String debugStr = prefix + " tupleSpace: " + tupleSpace
                + "\n" + prefix + " takeEvents: " + takeEvents
                + "\n" + prefix + " readEvents: " + readEvents;
        System.out.println(debugStr);
    }

}
