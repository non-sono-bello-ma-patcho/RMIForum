package Server;

import core.MessageClass;

import java.util.HashMap;
import java.util.List;

public class RMIServer implements core.RMIServer {
    private HashMap<String, List<MessageClass>> Topics;
    private List<String> ClientList;
    private String CurrentTopic;
    @Override
    public boolean ManageConnection(String username, String password, String op) {
        return false;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, boolean unsubscribe) {
        return false;
    }

    @Override
    public void Notify() {
        // call remotely users methods....
    }

    @Override
    public void ManagePublish(MessageClass msg) {
        Topics.get(CurrentTopic).add(msg);
        Notify(); // update local users convos...
    }
}
