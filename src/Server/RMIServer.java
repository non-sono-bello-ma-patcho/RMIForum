package Server;

import core.MessageClass;
import core.RMIClient;
import core.TopicClass;

import java.util.HashMap;
import java.util.List;

public class RMIServer implements core.RMIServer {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    final int clientPort = 1968;

    @Override
    public boolean ManageConnection(String username, String password, String op) {
        return false;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) {
        if(!unsubscribe) return Topics.get(TopicLabel).addUser(User);
        else return Topics.get(TopicLabel).RemoveUser(User);
    }

    @Override
    public void Notify() {
        // call remotely users methods....
    }

    @Override
    public void ManagePublish(MessageClass msg, String TopicName) {
        Topics.get(TopicName).addMessage(msg);
        Notify(); // update local users convos...
    }
}
