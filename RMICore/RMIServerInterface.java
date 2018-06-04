package RMIForum.RMICore;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/* sentiti libero di fare le modifiche che vuoi.. ho solo messo per ora dei parametri che mi venivano bene nel lato client*/
public interface RMIServerInterface extends Remote {
    boolean ManageConnection(String username, String password, String address, String op) throws RemoteException;
    boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException; /* metodo per iscrizione a topic... manca la dichiarazione di un metdodo per iscrizione al forum stesso*/
    boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException; // add message to  a topic convo
    boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException;
    ConcurrentHashMap<String, TopicClass> getTopics() throws RemoteException;
}
