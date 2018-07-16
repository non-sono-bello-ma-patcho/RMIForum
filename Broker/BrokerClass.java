package RMIForum.Broker;

import RMIForum.RMICore.*;
import RMIForum.Server.RMIServer;
import RMIForum.user.User;

import java.util.ArrayList;
import java.util.List;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BrokerClass extends User implements RMIServerInterface {
    private TopicList Topics;
    protected ConcurrentHashMap<String, RMIClient> ClientList; // TODO: to wrap into a class;
    private List<String> ChildrenIDs;
    private PoolClass pool;
    private RMIUtility serverHandler;
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    public BrokerClass() throws UnknownHostException {
        super();
        Topics = new TopicList();
        ClientList = new ConcurrentHashMap<>();
        pool = new PoolClass();
        ChildrenIDs = new ArrayList<>();
        serverHandler = new RMIUtility(1969, "RMISharedServer", "RMISharedClient");
    }

    /*-- Server Side --*/
    /*-- Make all this functions selective --*/



    /*
    @Override
    public static void printInfo(RMIServer rs){  //static void generates problems with overriding
      super.printInfo(rs);
    }
    */
    /** USER SIDE **/
    @Override
    public void CLiNotify(String TopicLabel,String TriggeredBy, boolean type) throws RemoteException {
        super.CLiNotify(TopicLabel, TriggeredBy, type);
        System.err.println("FORWARDING NOTIFICATION");
        Notify(TopicLabel, TriggeredBy, type);
    }

    /** SERVER SIDE **/


    public void ListFiller(List<String> l){
        for(String s : l){
            ChildrenIDs.add(s);
        }
    }

    public boolean isNotChild(List<String> l){
        for(String s : l)
            if(ChildrenIDs.contains(s))
                return false;
        return true;

    }

    public void removeIds(List<String> l){
        for(String s : l)
            ChildrenIDs.remove(s);
    }

    @Override
    public ConnResponse ManageConnection(String username, RMIClient stub, List<String>BrokerID,String op) throws RemoteException {
        switch (op) {
            case "connect":
                if (ClientList.containsKey(username)) return ConnResponse.AlreadyExist;
                printDebug("Adding [" + username + "] to Users!");
                // init conversation with client...
                printDebug("Trying to retrieve stub from "+username);
                if(BrokerID != null){
                    if(isNotChild(BrokerID)) {
                        ListFiller(BrokerID);
                    }
                    else return ConnResponse.cyclicityDetected;
                }
                ClientList.putIfAbsent(username, stub);
                break;
            case "disconnect":
                if (!ClientList.containsKey(username)) return ConnResponse.NoSuchUser;
                printDebug("Removing [" + username + "] from Users:");
                if(BrokerID != null) removeIds(BrokerID);
                printDebug(username + (kickUser(username)?" removed" : " not removed"));
                break;
        }
        return ConnResponse.Success;
    }

    @Override
    public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
        printDebug("["+User+"] wants to "+(!unsubscribe?"subscribe to ":"unsubscribe from ")+" ["+TopicLabel+"]: ");
        if(!Topics.contains(TopicLabel)){
            printDebug("No such topic...");
            if(!GetConnectonStatus())return false;
            else return ServerConnected.ManageSubscribe(TopicLabel, User, unsubscribe);
        }
        if(!unsubscribe) return (Topics.getTopicNamed(TopicLabel)).addUser(User);
        else return Topics.getTopicNamed(TopicLabel).RemoveUser(User);
    }

    @Override
    public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
        // verifico che il topic non sia su un altro broker...
        if(!Topics.contains(TopicName)){
            // cerco il topic sull'altro broker e all'occorrenza chiamo ManagePublish su quello...
            if(!GetConnectonStatus()) return false;
            return ServerConnected.ManagePublish(msg, TopicName);
        }
        if (!Topics.getTopicNamed(TopicName).hasUser(msg.getUser())) return false;
        printDebug("Publishing |" + msg.getFormatMsg() + "| to [" + TopicName + "]!");
        (Topics.getTopicNamed(TopicName)).addMessage(msg);
        Notify(TopicName, msg.getUser(), true); // update local users convos...
        return true;
    }

    @Override
    public TopicList getTopics() throws RemoteException {
        return Topics;
    }

    public List<String> getConnectedUsers() {
        return new ArrayList<>(ClientList.keySet());
    } //todo: add in main repo.

    public List<String> getChildrenIDs(){return ChildrenIDs;}

    @Override
    public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException {
        if(Topics.contains(TopicName)) return false;
        printDebug("Adding ["+TopicName+"] to Topics!");
        Topics.put(new TopicClass(TopicName, TopicOwner));
        Notify(TopicName, TopicOwner, false);
        return true;
    }

    @Override
    public boolean ImConnected(String user) throws RemoteException {
        return ClientList.containsKey(user);
    }

    public boolean removeMessage(String TopicLabel, String message){
        return Topics.getTopicNamed(TopicLabel).removeMessage(message);
    }

    private boolean kickUser(String user){
        if(!ClientList.containsKey(user)) return false;
        ClientList.remove(user);
        if(ChildrenIDs.contains(user)) ChildrenIDs.remove(user);
        return true;
    }

    public boolean ManualkickUser(String user) throws ExecutionException, InterruptedException {
        if(!ClientList.containsKey(user)) return false;
        Future<String> response = notifyClient(user, ClientList.get(user), "_REMOVE_", "", false);
        response.get();
        ClientList.remove(user);
        if(ChildrenIDs.contains(user)) ChildrenIDs.remove(user);
        return true;
    }

    public boolean removeTopic(String TopicLabel){
        if(!Topics.contains(TopicLabel)) return false;
        Topics.remove(TopicLabel);
        // TODO: notify clients you removed the topic...
        return true;
    }

    public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        BrokerClass tmp = this;
        boolean hasu = false;
        List<Future<String>> response = new ArrayList<>(ClientList.size());
        for (String s : ClientList.keySet()) {
            // devo verificare che il topic in questione non sia su un altro broker...
            while(tmp.GetConnectonStatus()){
                if(tmp.Topics.contains(TopicLabel)){
                    hasu = tmp.Topics.getTopicNamed(TopicLabel).hasUser(s);
                    break;
                }
                tmp = (BrokerClass) tmp.ServerConnected;
            }
            if (hasu || !type) { // notify only if a topic has been added or the user is subscribed...
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

    public void start(String Host){
        serverHandler.serverSetUp(this, Host);
    }

    public void shutDown() throws RemoteException, NotBoundException {
        serverHandler.RMIshutDown(this);
        pool.StopPool();
    }

    private void printDebug(String text){
        System.err.println("[ServerDebug]: "+text);
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
