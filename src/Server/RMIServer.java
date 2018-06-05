package Server;

import RMICore.*;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RMIServer implements RMIServerInterface {
    private TopicList Topics; // TODO: to wrap into a class;
    private ConcurrentHashMap<String, RMIClient> ClientList; // TODO: to wrap into a class;
    private ConcurrentHashMap<String, String> Credential; // TODO: incorporate into clientlist class...
    private PoolClass pool;
    private String myHost;
    private RMIUtility serverHandler;
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    public RMIServer(String Host) {
        Topics = new TopicList();
        ClientList = new ConcurrentHashMap<>();
        Credential = new ConcurrentHashMap<>();
        pool = new PoolClass();
        serverHandler = new RMIUtility(1969, 1099, "RMISharedServer", "RMISharedClient");
        myHost = Host;
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
                } catch (ConnectException e) {
                    System.err.println("Host hasn't set its policy...");
                    return false;
                }catch (RemoteException e) {
                    System.err.println("Impossible to retrieve stub from client...");
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
                break;
        }
        return true;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        printDebug("["+User+"] wants to "+(unsubscribe?"subscribe to ":"unsubscribe from ")+" ["+TopicLabel+"]: ");
        if(!Topics.contains(TopicLabel)){
            printDebug("No such topic...");
            return false;
        }
        if(!unsubscribe) return (Topics.getTopicNamed(TopicLabel)).addUser(User);
        else return Topics.getTopicNamed(TopicLabel).RemoveUser(User);
    }

    @Override
    public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
        if(!Topics.getTopicNamed(TopicName).hasUser(msg.getUser())) return false;
        printDebug("Publishing |"+msg.getFormatMsg()+"| to ["+TopicName+"]!");
        (Topics.getTopicNamed(TopicName)).addMessage(msg);
        Notify(TopicName, msg.getUser(), true); // update local users convos...
        return true;
    }

    @Override
    public TopicList getTopics() throws RemoteException {
        return Topics;
    }

    @Override
    public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException {
        if(Topics.contains(TopicName)) return false;
        System.err.println("Adding ["+TopicName+"] to Topics!");
        Topics.put(new TopicClass(TopicName, TopicOwner));
        Notify(TopicName, TopicOwner, false);
        return true;
    }

    public static void printInfo(RMIServer rs){ /*should it be right for the client class too?*/
        System.out.println("Available Topics:");
        for(String t : rs.Topics.ListTopicName()) System.out.println(t);

        System.out.println("Connected users:");
        for(String t : rs.ClientList.keySet()) System.out.println(t);

        System.out.println("Topics and messages:");
        for(String t : rs.Topics.ListTopicName()){
            System.out.println("Topic ["+t+"]:");
            List<String> messages = rs.Topics.getTopicNamed(t).ListMessages();
            for(String m : messages) System.out.println("\t"+m);
        }
    }

    public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        List<Future<String>> response = new ArrayList<>(ClientList.size());
        for (String s : ClientList.keySet()) {
            if (Topics.getTopicNamed(TopicLabel).hasUser(s) || !type) { // notify only if a topic has been added or the user is subscribed...
                printDebug("Notifying [" + s + "]:");
                response.add(notifyClient(s, ClientList.get(s), TopicLabel, TriggeredBy, type));
            }
        }
        for (Future<String> f : response) {
            String result = null;
            try {
                result = f.get();
            } catch (InterruptedException e) {
                e.printStackTrace(); // TODO: Handle exception
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(!result.equals("Reached")) {
                printDebug("Impossible to invoke CliNotify from " + result + ": removing it from clients:");
                ManageConnection(result, null, null, "disconnect");
            }
        }
    }

    public void start(){
        serverHandler.serverSetUp(this, myHost);
    }

    public void shutDown() throws RemoteException, NotBoundException {
        serverHandler.RMIshutDown(this);
        pool.StopPool();
    }

    public static void main(String [] args) {
        RMIServer rs = new RMIServer(args[0]);
        rs.start();
        // here start the server...
        System.out.println("Type something to shutdown...");
        Scanner sc = new Scanner(System.in);
        System.err.println("You typed: "+sc.next());
        try {
            rs.shutDown();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        printInfo(rs);
    }

    private void printDebug(String text){
        System.err.println(ANSI_BLUE+"[Debug]: "+text+ANSI_RESET);
    }

    class notifyHandler implements Callable {
        String username, topiclabel, triggeredby;
        boolean type;
        RMIClient stub;
        public notifyHandler(String user, RMIClient userstub, String tl, String tb, boolean t){
            username = user;
            stub = userstub;
            topiclabel = tl;
            triggeredby = tb;
            type = t;
        }
        @Override
        public String call() {
            try {
                stub.CLiNotify(topiclabel, triggeredby, type);
            } catch (RemoteException e) {
                return username;
            }
            return "Reached";
        }
    }

    private Future notifyClient(String user, RMIClient userstub, String tl, String tb, boolean t){
        return pool.submit(new notifyHandler(user, userstub, tl, tb, t));
    }
}
