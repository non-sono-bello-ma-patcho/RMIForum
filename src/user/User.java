package user;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
    public Registry pushRegistry; /* Registry used for pushing remote method */
    public RMIServer ServerConnected; /* that is the stub */
    private String usurname;
    private String pswd;
    private boolean connected = false;
    private final int myListeningPort = 1968;
    private HashMap<String, TopicClass> ServerTopics;
    private HashMap<String, Boolean> myTopics;
    private HashMap<String,List<String>> TopicMessages;

    public User(String nick, String password, int port){
        usurname = nick;
        pswd = password;
    }


    /*method that try to connect/disconnect to the rmi server */
    private  boolean ConnectionRequest(String host, String op){
        switch(op){
            case "connect":
                if ( connected == true){
                    System.err.println("You are already connected");
                    return false;
                }
                try {
                    pullRegistry = LocateRegistry.getRegistry(host);
                    ServerConnected = (RMIServer) pullRegistry.lookup("RMISharedClient");
                    /*remember to switch the lookup parameter with the right one */
                    InetAddress ia = InetAddress.getLocalHost();
                    boolean result = ServerConnected.ManageConnection(usurname,pswd,ia.getHostAddress(),op);

                    /*sostanzialmente questa parte l'ho dedicata all'inizializzazione delle hashmap */
                    if(result == true ){
                        connected = true;
                        ServerTopics = ServerConnected.getTopics(); /*inizializzo la hashmap ServerTopics */
                        Set<String> mySetofKey = ServerTopics.keySet(); /* mi è utile per usare l'iterator */
                        Iterator<String> myIterator = mySetofKey.iterator();
                        while(myIterator.hasNext()){
                            String TopicName = ServerTopics.get(myIterator).getName();
                            myTopics.put(TopicName,false); /* inizializzo la hashmap contenete i miei topics (di default sono tutti false a connection time ) */
                            TopicMessages.put(TopicName,ServerTopics.get(TopicName).ListMessages()); /* inizializzo la mia hashmap contenente ogni topic coi relativi mex */
                            myIterator.next();
                        }
                    }

                    return result;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                if(connected == false){
                    System.err.println("You are already disconnected");
                    return true;
                }
                try{
                    InetAddress ia = InetAddress.getLocalHost();
                    boolean result = ServerConnected.ManageConnection(usurname,pswd,ia.getHostAddress(),op);
                    if(result == true ) connected = false;
                    return result;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            default:
                System.err.println("invalid operation");
                break;
        }
        return false;
    }

/* that method is for the topic registration..*/
    private boolean SubscribeRequest(String TopicName, String op){
        if(connected == false){
            System.err.println("Permission denied! you are not connected!");
            return false;
        }
        switch(op){
            case "subscribe":
                myTopics.replace(TopicName,true); /* indico nella mia hashmap che mi sono iscritto anche a quel topic */
                return ServerConnected.ManageSubscribe(usurname,TopicName,false); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            case "unsubscribe":
                myTopics.replace(TopicName,false); /* indico nella mia hashmap che mi sono disiscritto anche a quel topic */
                return ServerConnected.ManageSubscribe(usurname,TopicName,true); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            default:
                System.err.println("invalid operation");
        }
        return false; /*something gone wrong, probably wrong operation*/
        /*if the topic doesn't exist, maybe the server could create it and subscribe that user and than return (?)*/
    }


    private boolean MessageRequest(MessageClass msg,String topicName){
        if(connected == false){
            System.err.println("Permission denied! you are not connected!");
            return false;
        }
        ServerConnected.ManagePublish(msg,topicName);
        return true;
    }

    public void CLiNotify(){
        if(connected == false){
            System.err.println("Permission denied! The client isn't connected");
            return;
        }
        ServerTopics = ServerConnected.getTopics();

        /*check if the notify has come for a new topic creation */
        Set<String> setTopic = ServerTopics.keySet();
        Iterator<String> myIterator = setTopic.iterator();
        while(myIterator.hasNext()){
            String TopicName = ServerTopics.get(myIterator).getName();
            if(!myTopics.containsKey(myIterator)) {
                System.out.println("Flamingorum has recently added:" + TopicName + " topic");
                //String topicToAdd = ServerTopics.get(myIterator).getName();
                myTopics.put(TopicName, false);
                TopicMessages.put(TopicName,ServerTopics.get(TopicName).ListMessages());
            }
            else if(myTopics.get(TopicName) == true){
                if(ServerTopics.get(TopicName).ListMessages().size() > TopicMessages.get(TopicName).size()) {
                    TopicMessages.replace(TopicName, ServerTopics.get(TopicName).ListMessages());
                    System.out.println("There are new messages on " + TopicName + " topic");
                }
            }

            myIterator.next();
        }
    }

    private void remoteExportation(User myUser){
        try {
            RMIClient Stub = (RMIClient) UnicastRemoteObject.exportObject(myUser,0);
            pushRegistry = LocateRegistry.createRegistry(myListeningPort);
            pushRegistry.bind("RMISharedClient",Stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    public String GetUsername(){
        return this.usurname;
    }

    public String GetPassword(){
        return this.pswd;
    }

    public boolean GetConnectonStatus(){
        return this.connected;
    }

    public static void main(String[] args){

    }
}
