package linda.shm;

import linda.Callback;
import linda.Tuple;

/**
 * Représente un "match" en attente avec son template et callback
 */
public class Event {

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
