package core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/* sentiti libero di fare le modifiche che vuoi.. ho solo messo per ora dei parametri che mi venivano bene nel lato client*/
public interface RMIServer extends Remote {
    boolean ManageConnection(String username, String password, String address, String op) throws RemoteException;
    boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException; /* metodo per iscrizione a topic... manca la dichiarazione di un metdodo per iscrizione al forum stesso*/
    void Notify() throws RemoteException; // triggers message show on client...
    void ManagePublish(MessageClass msg, String TopicName) throws RemoteException; // add message to  a topic convo
    HashMap<String, TopicClass> getTopics() throws RemoteException;
}
