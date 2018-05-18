package core;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClient extends Remote {
    /*void ConnectionRequest();
    void SubscribeRequest();
    void MessageRequest();*/
    /*se ci pensi l'unico metodo remoto che il server chiamerà sarà quello di notifica*/
    void CLiNotify() throws RemoteException;
}
