package linda.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/**
 * Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it
 * is connected to.
 */
public class LindaClient implements Linda {

    /** Serveur Linda distant */
    private LindaServer server;

    /**
     * Initializes the Linda implementation.
     * 
     * @param serverURI the URI of the server, e.g.
     *                  "rmi://localhost:4000/LindaServer" or
     *                  "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
            server = (LindaServer) Naming.lookup(serverURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            server.write(t);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return server.take(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return server.read(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return server.tryTake(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return server.tryRead(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return server.takeAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return server.readAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            // On transforme le Callback local du client en RemoteCallback distant pour le
            // serveur via RemoteCallbackAdapter
            server.eventRegister(mode, timing, template, new RemoteCallbackAdapter(callback));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void debug(String prefix) {
        // Rien à débug
    }

}
