package Server;

import core.*;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServerInterface implements core.RMIServerInterface {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    private HashMap<String, String> Credential;
    private PoolClass pool;
    private Registry ServerRegistry;
    private final int clientPort = 1968;

    private void serverSetUp(){
        System.setProperty("java.security.policy", "/tmp/RMIServerInterface.policy");
        if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        // RMIServerInterface obj = new RMIServerInterface();
        String alias = "RMISharedServer";
        try {
            ServerRegistry=setRegistry(clientPort);
            ExportNBind(ServerRegistry, this, alias,clientPort);

            System.err.println("Server ready, type something to shutdown...");
            Scanner sc = new Scanner(System.in);
            System.err.println("You typed: "+sc.next());
        } catch (RemoteException e) {
            System.err.println("Couldn't set registry, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (AlreadyBoundException e) {
            System.err.println("Couldn't export and bind, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        }
    }

    static void showStackTrace(Exception e){
        /*Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n')*/ e.printStackTrace();
    }

    private RMIClient getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from host...");
        Registry registry = LocateRegistry.getRegistry(host, clientPort);
        System.err.println("LookingUp for share Object");
        return (RMIClient) registry.lookup("RMISharedClient");
    }

    private Registry setRegistry(int port) throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(port);
        }
    }

    private void ExportNBind(Registry reg, RMIServerInterface obj, String alias, int port) throws AlreadyBoundException, RemoteException {
        RMIServerInterface stub = (RMIServerInterface) UnicastRemoteObject.exportObject(obj, port);
        reg.bind(alias, stub);
    }

    public void RMIshutDown() throws RemoteException, NotBoundException {
        ServerRegistry.unbind("RMISharedServer");
        UnicastRemoteObject.unexportObject(this, true);
    }

    public RMIServerInterface(){
        Topics = new HashMap<>();
        ClientList = new HashMap<>();
        Credential = new HashMap<>();
        pool = new PoolClass();

        // here start the server...
        serverSetUp();
    }

    public void shutDown() throws RemoteException, NotBoundException {
        RMIshutDown();
        pool.StopPool();
    }

    @Override
    public synchronized boolean ManageConnection(String username, String password, String address, String op) throws RemoteException {
        // init conversation with client...
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
        // disconnection need to be added
    }

    @Override
    public synchronized boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        if(!unsubscribe) return Topics.get(TopicLabel).addUser(User);
        else return Topics.get(TopicLabel).RemoveUser(User);
    }

    @Override
    public void Notify() {
        // call remotely users methods for all client registered...0
        // submit callable for each client....

    }

    @Override
    public void ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
        Topics.get(TopicName).addMessage(msg);
        Notify(); // update local users convos...
    }

    @Override
    public HashMap<String, TopicClass> getTopics() throws RemoteException {
        return Topics;
    }

    public static void main(String [] args){
        RMIServerInterface rs = new RMIServerInterface();
    }
}
