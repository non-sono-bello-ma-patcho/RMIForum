package core;

import java.rmi.Remote;

/* seniti libero di fare le modifiche che vuoi.. ho solo messo per ora dei parametri che mi venivano bene nel lato client*/
public interface RMIServer extends Remote {
    boolean ManageConnection(String username,String password, String address, String op);
    boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe); /* metodo per iscrizione a topic... manca la dichiarazione di un metdodo per iscrizione al forum stesso*/
    void Notify(); // triggers message show on client...
    void ManagePublish(MessageClass msg, String TopicName); // add message to  a topic convo
}
