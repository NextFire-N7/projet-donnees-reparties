package linda.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.shm.CentralizedLinda;

public class LindaServerImpl extends UnicastRemoteObject implements LindaServer {

    private Linda kernel;

    public LindaServerImpl() throws RemoteException {
        kernel = new CentralizedLinda();
    }

    @Override
    public void write(Tuple t) throws RemoteException {
        System.out.println("write: " + t);
        kernel.write(t);
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException {
        System.out.println("take: " + template);
        return kernel.take(template);
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {
        System.out.println("read: " + template);
        return kernel.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        System.out.println("tryTake: " + template);
        return kernel.tryTake(template);
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        System.out.println("tryRead: " + template);
        return kernel.tryRead(template);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        System.out.println("takeAll: " + template);
        return kernel.takeAll(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        System.out.println("readAll: " + template);
        return kernel.readAll(template);
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, RemoteCallback remoteCallback)
            throws RemoteException {
        System.out.println("eventRegister: " + mode + " " + timing + " " + template);
        kernel.eventRegister(mode, timing, template, t -> {
            try {
                remoteCallback.call(t);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        try {
            LindaServer lindaServer = new LindaServerImpl();
            LocateRegistry.createRegistry(4000);
            Naming.rebind("//localhost:4000/LindaServer", lindaServer);
            System.out.println("LindaServer ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
