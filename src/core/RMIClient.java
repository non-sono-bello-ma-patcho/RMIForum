package core;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClient extends Remote {
    void CLiNotify() throws RemoteException;
}
