package Server;

import RMICore.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RMIServer implements RMIServerInterface {
    private HashMap<String, TopicClass> Topics;
    private HashMap<String, RMIClient> ClientList;
    private HashMap<String, String> Credential;
    private PoolClass pool;
    private final int serverPort = 1969;
    private final int clientPort = 1099;
    private String myHost;
    private RMIUtility serverHandler;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";

    public RMIServer(String Host) {
        Topics = new HashMap<>();
        ClientList = new HashMap<>();
        Credential = new HashMap<>();
        pool = new PoolClass();
        serverHandler = new RMIUtility(serverPort, clientPort, "RMISharedServer", "RMISharedClient");
        myHost = Host;
    }

    public void start(){
        serverHandler.serverSetUp(this, myHost);
    }

    public void shutDown() throws RemoteException, NotBoundException {
        serverHandler.RMIshutDown(this);
        pool.StopPool();
    }

    @Override
    public boolean ManageConnection(String username, String password, String address, String op) throws RemoteException {
        switch (op) {
            case "connect":
                if (ClientList.containsKey(username)) return false;
                System.err.println("Adding [" + username + "] to Users!");
                // init conversation with client...
                try {
                    System.err.println("Trying to retrieve methods from " + address);
                    RMIClient stub = (RMIClient) serverHandler.getRemoteMethod(address);
                    System.err.println("DONE");
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
                break;
            case "disconnect":
                if (!ClientList.containsKey(username)) return false;
                System.err.print("Removing [" + username + "] from Users:");
                ClientList.remove(username);
                Credential.remove(username);
                System.err.println("DONE");
                break;
        }
        return true;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        printDebug("["+User+"] wants to "+(unsubscribe?"subscribe to ":"unsubscribe from ")+" ["+TopicLabel+"]: ");
        if(!Topics.containsKey(TopicLabel)){
            printDebug("No such topic...");
            return false;
        }
        if(!unsubscribe) return (Topics.get(TopicLabel)).addUser(User);
        else return Topics.get(TopicLabel).RemoveUser(User);
    }

    public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        for(String s : ClientList.keySet()){
            if(Topics.get(TopicLabel).hasUser(s) || !type) { // notify only if a topic has been added or the user is subscribed...
                printDebug("Notifying [" + s + "]:");
                try {
                    ClientList.get(s).CLiNotify(TopicLabel, TriggeredBy, type);
                } catch (RemoteException e) {
                    printDebug("Impossible to invoke CliNotify from "+s+": removing it from clients:");
                    ManageConnection(s, null, null, "disconnect");
                    printDebug("DONE");
                }
                System.err.println("DONE");
            }
        }
    }

    @Override
    public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
        if(!Topics.get(TopicName).hasUser(msg.getUser())) return false;
        printDebug("Publishing |"+msg.getFormatMsg()+"| to ["+TopicName+"]!");
        (Topics.get(TopicName)).addMessage(msg);
        Notify(TopicName, msg.getUser(), true); // update local users convos...
        return true;
    }

    @Override
    public HashMap<String, TopicClass> getTopics() throws RemoteException {
        return Topics;
    }

    @Override
    public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException {
        if(Topics.containsKey(TopicName)) return false;
        System.err.println("Adding ["+TopicName+"] to Topics!");
        Topics.put(TopicName, new TopicClass(TopicName, TopicOwner));
        Notify(TopicName, TopicOwner, false);
        return true;
    }

    public static void printInfo(RMIServer rs){ /*should it be right for the client class too?*/
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
        RMIServer rs = new RMIServer(args[0]);
        rs.start();
        // here start the server...
        System.out.println("Type something to shutdown...");
        Scanner sc = new Scanner(System.in);
        System.err.println("You typed: "+sc.next());
        try {
            rs.shutDown();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        printInfo(rs);
    }

    private void printDebug(String text){
        System.err.println(ANSI_BLUE+"[Debug]: "+text+ANSI_RESET);
    }
}
