package Server;

import core.MessageClass;
import core.RMIClient;
import core.TopicClass;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;

public class RMIServer implements core.RMIServer {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    final int clientPort = 1968;

    private RMIClient getRemoteMethod(String host, int port) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from host...");
        Registry registry = LocateRegistry.getRegistry(host, port);
        System.err.println("LookingUp for share Object");
        return (RMIClient) registry.lookup("RMISharedClient");
    }

    @Override
    public boolean ManageConnection(String username, String password, String op) {
        // init convo with client...

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
