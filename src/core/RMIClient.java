package core;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClient extends Remote {
    void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException;
}
