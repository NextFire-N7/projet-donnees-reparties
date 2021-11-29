package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;

import linda.AsynchronousCallback;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    /**
     * Event
     */
    public class Event {

        private Tuple template;
        private Callback callback;

        public Event(Tuple template, Callback callback) {
            this.template = template;
            this.callback = callback;
        }

        public boolean matchTest(Tuple tuple) {
            return tuple.matches(template);
        }

        public void call(Tuple tuple) {
            callback.call(tuple);
        }

        @Override
        public String toString() {
            return template.toString();
        }

    }

    private List<Tuple> tupleSpace;
    private List<Event> readEvents;
    private List<Event> takeEvents;

    public CentralizedLinda() {
        tupleSpace = new CopyOnWriteArrayList<>();
        readEvents = new CopyOnWriteArrayList<>();
        takeEvents = new CopyOnWriteArrayList<>();
    }

    @Override
    public void write(Tuple t) {
        for (Event rEvent : readEvents) {
            if (rEvent.matchTest(t)) {
                readEvents.remove(rEvent);
                rEvent.call(t);
            }
        }
        for (Event tEvent : takeEvents) {
            if (tEvent.matchTest(t)) {
                takeEvents.remove(tEvent);
                tEvent.call(t);
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
        SynchronousQueue<Tuple> queue = new SynchronousQueue<>(true);
        try {
            eventRegister(mode, eventTiming.IMMEDIATE, template, new AsynchronousCallback(t -> {
                try {
                    queue.put(t);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
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
        for (Tuple tuple : tupleSpace) {
            if (tuple.matches(template)) {
                if (mode == eventMode.TAKE) {
                    tupleSpace.remove(tuple);
                }
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
        if (timing == eventTiming.IMMEDIATE) {
            Tuple match = tryTakeRead(template, mode);
            if (match != null) {
                callback.call(match);
                return;
            }
        }
        Event event = new Event(template, callback);
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
