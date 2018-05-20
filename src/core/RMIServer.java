package core;
/* seniti libero di fare le modifiche che vuoi.. ho solo messo per ora dei parametri che mi venivano bene nel lato client*/
public interface RMIServer {
    boolean ManageConnection(String usurname,String password, String op);
    boolean ManageSubscribe();
    boolean ManageTopicSubscribe(String TopicLabel); /* metodo per iscrizione a topic..*/
    void Notify();
    void ManagePublish();
}
