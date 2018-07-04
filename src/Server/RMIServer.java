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
    private TopicList Topics;
    private ConcurrentHashMap<String, RMIClient> ClientList; // TODO: to wrap into a class;
    private PoolClass pool;
    private String myHost;
    private RMIUtility serverHandler;
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    public RMIServer(String Host) {
        Topics = new TopicList();
        ClientList = new ConcurrentHashMap<>();
        pool = new PoolClass();
        serverHandler = new RMIUtility(1969, "RMISharedServer", "RMISharedClient");
        myHost = Host;
    }

    @Override
    public ConnResponse ManageConnection(String username, RMIClient stub, String op) throws RemoteException {
        switch (op) {
            case "connect":
                if (ClientList.containsKey(username)) return ConnResponse.AlreadyExist;
                printDebug("Adding [" + username + "] to Users!");
                // init conversation with client...
                printDebug("Trying to retrieve stub from "+username);
                ClientList.putIfAbsent(username, stub);

                printDebug("Added! Checking presence by calling notify on it");
                for(String s : ClientList.keySet()) System.err.println(s);
                try {
                    ClientList.get(username).CLiNotify("TestInvoke", "Server", true);
                } catch (NullPointerException e){
                    System.err.println("Connection failed...");
                    return ConnResponse.NoSuchUser;
                }

                break;
            case "disconnect":
                System.err.println("Utenti registrati prima della rimozione di "+username);
                for(String s : ClientList.keySet()) System.err.println(s);
                if (!ClientList.containsKey(username)) return ConnResponse.NoSuchUser;
                printDebug("Removing [" + username + "] from Users:");
                kickUser(username);
                break;
        }
        return ConnResponse.Success;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        printDebug("["+User+"] wants to "+(!unsubscribe?"subscribe to ":"unsubscribe from ")+" ["+TopicLabel+"]: ");
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
        printDebug("Adding ["+TopicName+"] to Topics!");
        Topics.put(new TopicClass(TopicName, TopicOwner));
        Notify(TopicName, TopicOwner, false);
        return true;
    }

    public boolean removeMessage(String TopicLabel, String message){
        return Topics.getTopicNamed(TopicLabel).removeMessage(message);
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

    public boolean kickUser(String user){
        if(!ClientList.containsKey(user)) return false;
        ClientList.remove(user);
        return true;
    }

    public boolean removeTopic(String TopicLabel){
        if(!Topics.contains(TopicLabel)) return false;
        Topics.remove(TopicLabel);
        // TODO: notify clients you removed the topic...
        return true;
    }

    private void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
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
                kickUser(result);
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

    class notifyHandler implements Callable<String> {
        String username, topiclabel, triggeredby;
        boolean type;
        RMIClient stub;
        private notifyHandler(String user, RMIClient userstub, String tl, String tb, boolean t){
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

    private Future<String> notifyClient(String user, RMIClient userstub, String tl, String tb, boolean t){
        return pool.submit(new notifyHandler(user, userstub, tl, tb, t));
    }
}
