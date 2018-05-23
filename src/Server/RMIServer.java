package Server;

import core.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

public class RMIServer implements core.RMIServerInterface {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    private HashMap<String, String> Credential;
    private PoolClass pool;
    private Registry ServerRegistry;
    private final int clientPort = 1969;

    private void serverSetUp(){
        System.setProperty("java.security.policy", "/tmp/RMIServer.policy");
        if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        // RMIServer obj = new RMIServer();
        String alias = "RMISharedServer";
        try {
            ServerRegistry=setRegistry(clientPort);
            ExportNBind(ServerRegistry, this, alias,clientPort);

            InetAddress ia = InetAddress.getLocalHost();
            System.err.println("Server up and running on:"+ia.getHostAddress()+", type something to shutdown...");
            Scanner sc = new Scanner(System.in);
            System.err.println("You typed: "+sc.next());
            RMIshutDown();
        } catch (RemoteException e) {
            System.err.println("Couldn't set registry, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (AlreadyBoundException e) {
            System.err.println("Couldn't export and bind, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (UnknownHostException e) {
            System.err.println("Couldn't get localhost, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (NotBoundException e) {
            System.err.println("Couldn't unbound, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);        }
    }

    static void showStackTrace(Exception e){
        /*Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n')*/ e.printStackTrace();
    }

    private RMIClient getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from host...");
        Registry registry = LocateRegistry.getRegistry(host, clientPort);
        System.err.print("LookingUp for share Object: ");
        return (RMIClient) registry.lookup("RMISharedClient");
    }

    private Registry setRegistry(int port) throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(port);
        }
    }

    private void ExportNBind(Registry reg, RMIServer obj, String alias, int port) throws AlreadyBoundException, RemoteException {
        RMIServerInterface stub = (RMIServerInterface) UnicastRemoteObject.exportObject(obj, port);
        reg.bind(alias, stub);
    }

    public void RMIshutDown() throws RemoteException, NotBoundException {
        ServerRegistry.unbind("RMISharedServer");
        UnicastRemoteObject.unexportObject(this, true);
        pool.StopPool();
    }

    public RMIServer(){
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
        System.err.println("Adding ["+username+"] to Users!");
        // init conversation with client...
        try {
            System.err.println("Trying to retrieve methods from "+address);
            RMIClient stub = getRemoteMethod(address);
            System.err.println("DONE");
            if(ClientList.containsKey(username)) return false;
            ClientList.putIfAbsent(username, stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.err.println("Looks like there no shared object on that server...");
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
        System.err.println("Adding ["+msg+"] to ["+TopicName+"]!");
        Topics.get(TopicName).addMessage(msg);
        Notify(); // update local users convos...
    }

    @Override
    public HashMap<String, TopicClass> getTopics() throws RemoteException {
        return Topics;
    }

    @Override
    public synchronized boolean addTopic(String TopicName, String TopicOwner){
        System.err.println("Adding ["+TopicName+"] to Topics!");
        if(Topics.containsKey(TopicName)) return false;
        Topics.put(TopicName, new TopicClass(TopicName, TopicOwner));
        return true;
    }

    public static void main(String [] args){
        RMIServer rs = new RMIServer();
    }
}
