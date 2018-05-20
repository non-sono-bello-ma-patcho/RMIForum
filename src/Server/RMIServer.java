package Server;

import core.MessageClass;
import core.RMIClient;
import core.TopicClass;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

public class RMIServer implements core.RMIServer {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    private HashMap<String, String> Credential;
    final int clientPort = 1968;

    private RMIClient getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from host...");
        Registry registry = LocateRegistry.getRegistry(host, clientPort);
        System.err.println("LookingUp for share Object");
        return (RMIClient) registry.lookup("RMISharedClient");
    }

    @Override
    public synchronized boolean ManageConnection(String username, String password, String address, String op) {
        // init convo with client...
        try {
            RMIClient stub = getRemoteMethod(address);
            if(ClientList.containsKey(username)) return false;
            ClientList.putIfAbsent(username, stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        Credential.put(username, password);
        return true;
    }

    @Override
    public synchronized boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) {
        if(!unsubscribe) return Topics.get(TopicLabel).addUser(User);
        else return Topics.get(TopicLabel).RemoveUser(User);
    }

    @Override
    public void Notify() {
        // call remotely users methods for all client registered...0
    }

    @Override
    public void ManagePublish(MessageClass msg, String TopicName) {
        Topics.get(TopicName).addMessage(msg);
        Notify(); // update local users convos...
    }
}
