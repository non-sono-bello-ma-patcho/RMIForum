package Server;

import core.*;
import user.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RMIServer implements core.RMIServerInterface {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    private HashMap<String, String> Credential;
    private PoolClass pool;
    private Registry ServerRegistry;
    private final int serverPort = 1969;
    private final int clientPort = 1099;
    private RMIUtility serverHandler;
    /*------------------------Auxiliary functions--------------------------*/
/*
    private void serverSetUp() throws UnknownHostException {
        System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        System.setProperty("java.security.policy", "/tmp/RMIServer.policy");
        if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        // RMIServer obj = new RMIServer();
        String alias = "RMISharedServer";
        try {
            ServerRegistry=setRegistry(serverPort);
            ExportNBind(ServerRegistry, this, alias,serverPort);

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
        Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n') e.printStackTrace();
    }

    private RMIClient getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from"+host+"...");
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
    }
*/
    /*----------------------------------------------------------------------*/

    public RMIServer() throws RemoteException, NotBoundException {
        Topics = new HashMap<>();
        ClientList = new HashMap<>();
        Credential = new HashMap<>();
        pool = new PoolClass();
        serverHandler = new RMIUtility(ServerRegistry, serverPort, clientPort, "RMISharedServer", "RMISharedClient");
        // here start the server...
        try {
            serverHandler.serverSetUp(this, InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            System.err.println("Couldn't setup server...");
        }
        Scanner sc = new Scanner(System.in);
        System.err.println("You typed: "+sc.next());
        shutDown();
    }

    public void shutDown() throws RemoteException, NotBoundException {
        serverHandler.RMIshutDown(this);
        pool.StopPool();
    }

    @Override
    public synchronized boolean ManageConnection(String username, String password, String address, String op) throws RemoteException {
        System.err.println("Adding ["+username+"] to Users!");
        // init conversation with client...
        try {
            System.err.println("Trying to retrieve methods from "+address);
            RMIClient stub = serverHandler.getRemoteMethod(address);
            System.err.println("DONE");
            if(ClientList.containsKey(username)) return false;
            ClientList.putIfAbsent(username, stub);
        } catch (RemoteException e) {
            System.err.println("Remote problems pal....");
            e.printStackTrace();
            return false;
        } catch (NotBoundException e) {
            System.err.println("Looks like there no shared object on that server...");
            return false;
        }
        Credential.put(username, password);
        return true;
        // disconnection need to be added
    }

    @Override
    public synchronized boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        System.err.print("["+User+"] wants to subscribe to ["+TopicLabel+"] Topic:");
        if(!Topics.containsKey(TopicLabel)){
            System.err.println("No such topic...");
            return false;
        }
        System.err.println("DONE");
        if(!unsubscribe) return (Topics.get(TopicLabel)).addUser(User);
        else return Topics.get(TopicLabel).RemoveUser(User);
    }

    @Override
    public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        // call remotely users methods for all client registered...0
        // submit callable for each client....
        System.err.println("Send notify to all clients:");
        for(String s : ClientList.keySet()){
            if(Topics.get(TopicLabel).hasUser(s) || !type) { // notify only if a topic has been added or the user is subscribed...
                System.err.print("Notifying [" + s + "]:");
                ClientList.get(s).CLiNotify(TopicLabel, TriggeredBy, type);
                System.err.println("DONE");
            }
        }
    }

    @Override
    public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
        System.err.println("Publishing |"+msg.getFormatMsg()+"| to ["+TopicName+"]!");
        if(!Topics.get(TopicName).hasUser(msg.getUser())) return false;
        (Topics.get(TopicName)).addMessage(msg);
        Notify(TopicName, msg.getUser(), true); // update local users convos...
        return true;
    }

    @Override
    public HashMap<String, TopicClass> getTopics() throws RemoteException {
        return Topics;
    }

    @Override
    public synchronized boolean addTopic(String TopicName, String TopicOwner) throws RemoteException {
        if(Topics.containsKey(TopicName)) return false;
        System.err.println("Adding ["+TopicName+"] to Topics!");
        Topics.put(TopicName, new TopicClass(TopicName, TopicOwner));
        Notify(TopicName, TopicOwner, false);
        return true;
    }

    public static void printInfo(RMIServer rs){
        System.out.println("Available Topics:");
        for(String t : rs.Topics.keySet()) System.out.println(t);

        System.out.println("Connected users:");
        for(String t : rs.ClientList.keySet()) System.out.println(t);

        System.out.println("Topics and messages:");
        for(String t : rs.Topics.keySet()){
            System.out.println("Topic ["+t+"]:");
            List<String> messages = rs.Topics.get(t).ListMessages();
            for(String m : messages) System.out.println("\t"+m);
        }
    }

    public static void main(String [] args) throws InterruptedException {
        RMIServer rs = null;
        try {
            rs = new RMIServer();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        printInfo(rs);
    }
}
