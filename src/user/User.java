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
    private String username;
    private String pswd;
    private boolean connected = false;
    private final int myListeningPort = 1099;
    private HashMap<String, TopicClass> ServerTopics;
    private HashMap<String, Boolean> myTopics;
    private HashMap<String,List<String>> TopicMessages;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public User(String nick, String password){
        username = nick;
        pswd = password;
        ServerTopics = new HashMap<>();
        myTopics = new HashMap<>();
        TopicMessages = new HashMap<>();
    }


    /*    auxiliary functions   */

    public void CheckConnection(){
        if(!connected){
            System.err.println("You are not connected, operation failed");
            System.exit(-1);
        }
    }

    private void ChargeData(){
        CheckConnection();
        System.out.println("Trying to charging data from the server.....");
        try {
            ServerTopics = ServerConnected.getTopics(); /*inizializzo la hashmap ServerTopics */
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Set<String> mySetofKey = ServerTopics.keySet(); /* mi è utile per usare l'iterator */
        Iterator<String> myIterator = mySetofKey.iterator();
        while(myIterator.hasNext()){
            String TopicName = myIterator.next();
            System.err.println("topic name : "+ TopicName);

            myTopics.put(TopicName,false); /* inizializzo la hashmap contenete i miei topics (di default sono tutti false a connection time ) */
            TopicMessages.put(TopicName,ServerTopics.get(TopicName).ListMessages()); /* inizializzo la mia hashmap contenente ogni topic coi relativi mex */
            /*myIterator.next();*/
        }
        System.out.println("DONE");


    }

    /*method that try to connect/disconnect to the rmi server */
    private  boolean ConnectionRequest(String host, String op) throws AlreadyBoundException,RemoteException {
        switch(op){
            case "connect":
                if (connected){
                    System.err.println("You are already connected");
                    return false;
                }
                System.out.println("Trying to connect to the server " + host + " ...");

                try {
                    pullRegistry = LocateRegistry.getRegistry("localhost", 8000);
                    ServerConnected = (RMIServerInterface) pullRegistry.lookup("RMISharedServer");
                    /*InetAddress ia = InetAddress.getLocalHost();*/
                    pushRegistry = setRegistry(myListeningPort);
                    ExportNBind(pushRegistry,this,"RMISharedClient",myListeningPort);
                    boolean result = ServerConnected.ManageConnection(username,pswd,"localhost",op);
                    if(result) {
                        connected = true;
                        System.out.println("DONE");
                        ChargeData(); /*initialize the hashmaps */
                    }
                    return result;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                CheckConnection(); /* if you are not connected how could you think to disconnect??*/
                try{
                    InetAddress ia = InetAddress.getLocalHost();
                    boolean result = ServerConnected.ManageConnection(username,pswd,ia.getHostAddress(),op);
                    if(result ){
                        connected = false;
                        remoteUnbound();
                    }
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
        CheckConnection();
        switch(op){
            case "subscribe":
                myTopics.replace(TopicName,true); /* indico nella mia hashmap che mi sono iscritto anche a quel topic */
                return ServerConnected.ManageSubscribe(TopicName,username,false); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            case "unsubscribe":
                myTopics.replace(TopicName,false); /* indico nella mia hashmap che mi sono disiscritto anche a quel topic */
                return ServerConnected.ManageSubscribe(TopicName,username,true); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            default:
                System.err.println("invalid operation");
        }
        return false; /*something gone wrong, probably wrong operation*/
        /*if the topic doesn't exist, maybe the server could create it and subscribe that user and than return (?)*/
    }

    private boolean AddTopicRequest(String TopicName) throws RemoteException {
        CheckConnection();
        return ServerConnected.addTopic(TopicName, username);
    }

    private boolean MessageRequest(MessageClass msg,String topicName) throws RemoteException {
        CheckConnection();
        ServerConnected.ManagePublish(msg,topicName);
        return true;
    }

    @Override
    public synchronized void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
       /* CheckConnection();*/
       /*System.out.println("Messagio del server!!!!!!!! la notify funge");*/


        /**************************************HO CAMBIATO I CAMPI DELLA CLINOTIFY, PERDONAMI******************************************/
        ServerTopics = ServerConnected.getTopics();
        if(ServerTopics.size() == 0){
            System.out.println(ANSI_GREEN+"[Server message] : Welcome to Flaminforum!" + ANSI_RESET);
            return;
        }

        Set<String> setTopic = ServerTopics.keySet();
        Iterator<String> myIterator = setTopic.iterator();
        while(myIterator.hasNext()){
            /*System.err.println(myIteratory);*/

            String TopicName = myIterator.next();

            if(!myTopics.containsKey(TopicName)) {
                System.out.println(ANSI_GREEN+"[Server Message] : Flamingorum has recently added the topic : " + TopicName + ANSI_RESET);
                myTopics.put(TopicName, false);
                TopicMessages.put(TopicName,ServerTopics.get(TopicName).ListMessages());
            }
            else if(myTopics.get(TopicName)){
                if(ServerTopics.get(TopicName).ListMessages().size() > TopicMessages.get(TopicName).size()) {
                    TopicMessages.replace(TopicName, ServerTopics.get(TopicName).ListMessages());
                    System.out.println(ANSI_GREEN+"[Server Message] : There are new messages on " + TopicName + " topic" + ANSI_RESET);
                }
            }

           /* myIterator.next();*/
        }

    }

    private void remoteUnbound(){
        try {
            UnicastRemoteObject.unexportObject(this,true);
            pushRegistry.unbind("RMISharedClient");

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }


    public String GetUsername(){
        return this.username;
    }

    public String GetPassword(){
        return this.pswd;
    }

    public boolean GetConnectonStatus(){
        return this.connected;
    }


    private Registry setRegistry(int port) throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(port);
        }
    }

    private void ExportNBind(Registry reg, User obj, String alias, int port) throws AlreadyBoundException, RemoteException {
        RMIClient stub = (RMIClient) UnicastRemoteObject.exportObject(obj, port);
        reg.bind(alias, stub);
    }


/*that main is only a debugging, satiric version */
    public static void main(String[] args) {



        System.setProperty("java.security.policy", "/home/shinon/IdeaProjects/RMIForum/src/user/RMIClient.policy");
        if(System.getSecurityManager()== null) System.setSecurityManager(new SecurityManager());

        try {
            System.out.println("Trying to set policy for the hostname " + java.net.InetAddress.getLocalHost().getHostAddress());
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        User myUser = new User("Mortino", "111");
        User NonnaPapera = new User ("Nonna Papera","scoiattolo22");
        try {
            if (!myUser.ConnectionRequest(args[0], "connect")) {
                System.err.println("Mortino,Something gone wrong,retry to connect");
                System.exit(-1);
            }
            if (myUser.AddTopicRequest("Gloryhole"))
                System.out.println("adding the Topic Gloeyhole");
            else System.out.println("The topic Gloryhole already exist");
            if (!myUser.SubscribeRequest("Gloryhole", "subscribe"))
                System.err.println("Something gone wrong,retry to subscribe on Gloryhole topic");
            else {
                MessageClass myMessage = new MessageClass("Mortino", "Sarebbe bello vedere i piedi di Re Julien da quel buchino!");
                if (!myUser.MessageRequest(myMessage, "Gloryhole")) {
                    System.err.println("Something gone wrong, message not sent to Gloryhole");

                }
                else System.out.println("message sent");
            }

            if (!myUser.ConnectionRequest(args[0], "disconnect")) {
                System.err.println("Mortino ,Something gone wrong,cannot disconnect from the server");
                System.exit(-1); /* that means that the server returned false*/
            }
            /*nonna papera test section*/
            /*
            if (NonnaPapera.ConnectionRequest(args[0], "connect") == false) {
                System.err.println("NonnaPapera,Something gone wrong,retry to connect");
                System.exit(-1);
            }
            if (NonnaPapera.AddTopicRequest("Gloryhole"))
                System.out.println("adding the Topic Gloryhole");
            else System.out.println("The Topic Gloryhole already exist");
            if (!myUser.SubscribeRequest("Gloryhole", "subscribe"))
            System.err.println(NonnaPapera.username + " Something gone wrong,retry to subscribe on Gloryhole topic");
            else{
                MessageClass myMessage = new MessageClass("NonnaPapera", "Gasp!!! cercando nella cronologia di Paperino Paperotto sono finita qui.");
                if (!myUser.MessageRequest(myMessage, "Gloryhole")) {
                    System.err.println(NonnaPapera.username+ " Something gone wrong, message not sent to Gloryhole");

                } else System.out.println("message sent");
            }
*/
        }catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}