package user;
import core.*;


import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;



public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
    public Registry pushRegistry; /* Registry used for pushing remote method */
    public RMIServerInterface ServerConnected; /* that is the stub */
    public core.RMIClient Stub;
    private String usurname;
    private String pswd;
    private boolean connected = false;
    private final int myListeningPort = 1968;
    private HashMap<String, TopicClass> ServerTopics;
    private HashMap<String, Boolean> myTopics;
    private HashMap<String,List<String>> TopicMessages;

    public User(String nick, String password){
        usurname = nick;
        pswd = password;
    }


    /*optimization function */
    /*that method charge the data from the connected server*/
    private void ChargeData(){
        if(connected == false){
            System.err.println("You are not connected, the client is unable to charge data!");
            return;
        }
        try {
            ServerTopics = ServerConnected.getTopics(); /*inizializzo la hashmap ServerTopics */
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Set<String> mySetofKey = ServerTopics.keySet(); /* mi è utile per usare l'iterator */
        Iterator<String> myIterator = mySetofKey.iterator();
        while(myIterator.hasNext()){
            String TopicName = ServerTopics.get(myIterator).getName();
            myTopics.put(TopicName,false); /* inizializzo la hashmap contenete i miei topics (di default sono tutti false a connection time ) */
            TopicMessages.put(TopicName,ServerTopics.get(TopicName).ListMessages()); /* inizializzo la mia hashmap contenente ogni topic coi relativi mex */
            myIterator.next();
        }

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
                    pullRegistry = LocateRegistry.getRegistry(host, 1968);
                    ServerConnected = (RMIServerInterface) pullRegistry.lookup("RMISharedServer");
                    InetAddress ia = InetAddress.getLocalHost();
                    boolean result = ServerConnected.ManageConnection(usurname,pswd,ia.getHostAddress(),op);
                    if(result == true ) {
                        connected = true;
                        ChargeData(); /*initialize the hashmaps */
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
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            default:
                System.err.println("invalid operation");
                break;
        }
        return false;
    }

/* that method is for the topic registration..*/
    private boolean SubscribeRequest(String TopicName, String op) throws RemoteException {
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

    private boolean AddTopicRequest(String TopicName) throws RemoteException {
        if(!connected){
            System.err.println("Permission denied! you are not connected!");
            return false;
        }
        return ServerConnected.addTopic(TopicName, usurname);
    }

    private boolean MessageRequest(MessageClass msg,String topicName) throws RemoteException {
        if(!connected){
            System.err.println("Permission denied! you are not connected!");
            return false;
        }
        ServerConnected.ManagePublish(msg,topicName);
        return true;
    }

    public void CLiNotify() throws RemoteException {
        if(!connected){
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
            InetAddress ia = InetAddress.getLocalHost();
            System.setProperty("java.rmi.server.hostname", ia.getHostAddress()); /* should it be lochalhost????*/
            System.setProperty("java.security.policy", "/home/shinon/IdeaProjects/RMIForum/src/user/RMIClient.policy");
            if(System.getSecurityManager()== null) System.setSecurityManager(new SecurityManager());

            Stub = (core.RMIClient) UnicastRemoteObject.exportObject(myUser,myListeningPort);
            pushRegistry = LocateRegistry.createRegistry(myListeningPort);
            pushRegistry.bind("RMISharedClient",Stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void remoteUnbound(User myUser){
        try {
            UnicastRemoteObject.unexportObject(myUser,true);
            pushRegistry.unbind("RMISharedClient");

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
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


    /*that methods below are made only for debugging */
    private void printMaps(){
        return;
    }





/*that main is only a debugging, satiric version */
    public static void main(String[] args){
        User myUser = new User("Shinon","Cavolfiore92");
        if(myUser.ConnectionRequest(args[0],"connect") == false){
            System.err.println("Something gone wrong,retry to connect");
            System.exit(-1);/* se non riesce a connettersi è inutile fare altri test*/
        }
        myUser.remoteExportation(myUser);
        try {
            if(myUser.SubscribeRequest("Gloryhole","subscribe") == false)
                System.err.println("Something gone wrong,retry to subscribe on Gloryhole topic");
            if(myUser.SubscribeRequest("Deepthroat","subscribe") == false)
                System.err.println("Something gone wrong,retry to subscribe on Deepthroat topic");
            if(myUser.SubscribeRequest("MamminePastorine","subscribe") == false)
                System.err.println("Something gone wrong,retry to subscribe on MamminePastorine topic ");
            else{
                MessageClass myMessage = new MessageClass("Shinon","Mammine scrivo in anonimo, nascosta dal nickname \" Shinon. "+
                        "   Ieri mio marito mi ha chiesto di leccargli il carciofo, secondo voi cosa intendeva?? perchè io non capendo " +
                        "sono andata a fare la spesa... non c'erano carciofi.. forse è per questo che non mi parla da 3 ore? "+
                        "vostra,Claudia");
                if(myUser.MessageRequest(myMessage,"MamminePastorine") == false){
                    System.err.println("Something gone wrong, message not sent to MamminePastorine");
                }
                if(myUser.SubscribeRequest("MamminePastorine","unsubscribe") == false){
                    System.err.println("unsubscribe operation to MamminePastorine topic failed");
                }
            }
            Thread.sleep(60000); /* wait for some notifies */
            if(myUser.ConnectionRequest(args[0],"disconnect") == false) {
                System.err.println("Something gone wrong.. cannot disconnect from the server");
                System.exit(-1);
            }
            myUser.remoteUnbound(myUser); /* unbound del registro */
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
