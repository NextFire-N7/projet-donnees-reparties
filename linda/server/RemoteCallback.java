package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import linda.Tuple;

/**
 * Callback when a tuple appears. Version distant RMI.
 */
public interface RemoteCallback extends Remote {

    /**
     * Callback when a tuple appears.
     * See Linda.eventRegister for details.
     * 
     * @param t the new tuple
     * @throws RemoteException
     */
    public void call(Tuple t) throws RemoteException;

}
