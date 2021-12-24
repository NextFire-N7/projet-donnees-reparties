package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import linda.Tuple;

/**
 * Callback when a tuple appears. Version distant RMI.
 */
public abstract class RemoteCallback extends UnicastRemoteObject {

    public RemoteCallback() throws RemoteException {
    }

    /**
     * Callback when a tuple appears.
     * See Linda.eventRegister for details.
     * 
     * @param t the new tuple
     * @throws RemoteException
     */
    public abstract void call(Tuple t) throws RemoteException;

}
