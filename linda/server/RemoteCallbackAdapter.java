package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import linda.Callback;
import linda.Tuple;

/**
 * Transforme un callback local en callback distant.
 */
public class RemoteCallbackAdapter extends UnicastRemoteObject implements RemoteCallback {

    private Callback callback;

    public RemoteCallbackAdapter(Callback callback) throws RemoteException {
        this.callback = callback;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        callback.call(t);
    }

}
