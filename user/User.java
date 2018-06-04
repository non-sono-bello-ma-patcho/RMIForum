package RMIFourm.user;


import RMICore.*;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
  //  public Registry pushRegistry; /* Registry used for pushing remote method */ INTERNO NELLA CLASSE RMIUtility, non viene mai chiamato...
    public RMIServerInterface ServerConnected;
    public RMICore.RMIClient Stub;
    private boolean connected = false;
    private final int myListeningPort = 1099;
    private final int serverPort = 1969;
    private String host;
    private String username;
    private String password;
    private RMIUtility ClientHandler;
    private ConcurrentHashMap<String, TopicClass> ServerTopics;
    private HashMap<String, List<MessageClass>> TopicsMessages;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";


    /*     constructor    */

    public User(String myHost) throws UnknownHostException {
        host = myHost;
        ClientHandler = new RMIUtility(myListeningPort,serverPort,"RMISharedClient","RMISharedServer");
        ServerTopics = new ConcurrentHashMap<>();
        TopicsMessages = new HashMap<>();
        ClientHandler.serverSetUp(this, host);
    }


    /*    auxiliary functions   */
    // TODO: turn this function so that i returns the connection state instead exit program...
    public void CheckConnection(){
        if(!connected){
            System.err.println("[Client Error Message] : You are not connected, operation failed");
            System.exit(-1);
        }
    }

    private void ChargeData() throws RemoteException{
        CheckConnection();
        System.out.println(ANSI_BLUE+ "[Client Message] : Trying to fetching data from the server....."+ANSI_RESET);
        ServerTopics = ServerConnected.getTopics();
        for(String k : ServerTopics.keySet()) {
            if (TopicsMessages.containsKey(k)) TopicsMessages.replace(k, ServerTopics.get(k).getMessagesAsMessage());
            else TopicsMessages.put(k, ServerTopics.get(k).getMessagesAsMessage());
        }
        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
    }

  /*              Principal functions           */

    private  boolean ConnectionRequest(String Serverhost,String user,String psw, String op) throws  RemoteException {
        switch(op){
            case "connect":
                if (connected){
                    System.err.println("[Client Error Message] : You are already connected");
                    return false;
                }
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to connect to the server " + Serverhost + " ..."+ANSI_RESET);
                username = user;
                password = psw;
                try {
                    pullRegistry = LocateRegistry.getRegistry(Serverhost, serverPort);
                    ServerConnected = (RMIServerInterface) pullRegistry.lookup("RMISharedServer");
                    connected = ServerConnected.ManageConnection(user, psw, this.host, op);
                    if (connected) {
                        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
                        ChargeData();
                    }
                    return connected;
                }catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to disconnect from the server..."+ANSI_RESET);
                CheckConnection();
                try {
                    if(ServerConnected.ManageConnection(username, password, this.host, op)) {
                        connected = false;
                        ClientHandler.RMIshutDown(this);
                        System.out.println(ANSI_BLUE + "[Client Message] : Done." + ANSI_RESET);
                        return true;
                    }
                }catch (NotBoundException e) {
                    e.printStackTrace();
                }
            default:
                System.err.println("[Client Error Message] : invalid operation");
        }
        return false;
    }


    private boolean SubscribeRequest(String TopicName, String op) throws RemoteException {
        CheckConnection();
        switch(op){
            case "subscribe":
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to subscribe to : "+TopicName+"..."+ANSI_RESET);
                return ServerConnected.ManageSubscribe(TopicName,username,false);
            case "unsubscribe":
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to unsubscribe to : "+TopicName+"..."+ANSI_RESET);
                return ServerConnected.ManageSubscribe(TopicName,username,true);
            default:
                System.err.println("[Client Error Message] : invalid operation");
        }
        return false;
    }

    private boolean AddTopicRequest(String TopicName) throws RemoteException {
        System.out.println(ANSI_BLUE+"[Client Message] : Trying to add the topic : "+TopicName+"..."+ANSI_RESET);
        CheckConnection();
        return ServerConnected.ManageAddTopic(TopicName, username);
    }

    private boolean PublishRequest(MessageClass msg, String TopicName) throws RemoteException {
        System.out.println(ANSI_BLUE+"[Client Message] : Trying to send the message on : "+TopicName+"..."+ANSI_RESET);
        CheckConnection();
        ServerConnected.ManagePublish(msg,TopicName);
        return true;
    }

    @Override
    public  void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        CheckConnection();
        // notifier.add(TopicLabel);
        if(type){
            if(!username.equals(TriggeredBy))
                System.out.println(ANSI_GREEN+"[Server Message] : You have a new message from "+TriggeredBy+" on "+TopicLabel + ANSI_RESET);
            else
                System.out.println(ANSI_GREEN+"[Server Message] : You have sent a new message on "+TopicLabel+ ANSI_RESET);
        }
        else{
            if(!username.equals(TriggeredBy))
                System.out.println(ANSI_GREEN+"[Server Message] : "+TriggeredBy+" has created a new topic :  "+TopicLabel+ ANSI_RESET);
            else
                System.out.println(ANSI_GREEN+"[Server Message] : You have created a new topic : "+TopicLabel+ ANSI_RESET);
        }
        ChargeData();
    }


    /*          getters          */

    public String GetUsername(){
        return this.username;
    }

    public String GetPassword(){
        return this.password;
    }

    public boolean GetConnectonStatus(){
        return this.connected;
    }

    /*      Debugging function     */
    private void PrintMap(){
        for(String k :  ServerTopics.keySet()){
            System.out.println("[Debugging] : Topic = "+"["+k+"]");
            for(MessageClass m : TopicsMessages.get(k))
            System.out.println("                          "+"["+m.getUser()+"] : "+m.getText());
        }
    }

    public static void main(String[] args) throws UnknownHostException,RemoteException{
        /*debug messages */
        MessageClass myMessage = new MessageClass("Mortino", "Sarebbe bello vedere i piedi di Re Julien da quel buchino!");
        MessageClass msg = new MessageClass("Mortino","Qualcuno mi risponde???");
        MessageClass msg2 = new MessageClass("Mortino","Sapete come si crea un \"Topic\"???");
        /* end messages */
        // TODO: modify constructor so that the localhost is passed as a parameter (this works only on loopback...)
        User myUser = new User("localhost");
        if (!myUser.ConnectionRequest(args[0],"Mortino","12345", "connect")) {
            System.err.println("[Client Error Message] : Mortino,Something gone wrong,retry to connect");
            System.exit(-1);
        }
        if (!myUser.AddTopicRequest("Gloryhole"))
            System.err.println("[Client Error Message] :The topic Gloryhole already exist");
        if (!myUser.AddTopicRequest("HelpCenter"))
            System.err.println("[Client Error Message] :The topic HelpCenter already exist");

        if (!myUser.SubscribeRequest("Gloryhole", "subscribe"))
            System.err.println("[Client Error Message] : Something gone wrong,retry to subscribe on Gloryhole topic");
        else {
            if (!myUser.PublishRequest(myMessage, "Gloryhole")) {
                System.err.println("[Client Error Message] :Something gone wrong, message not sent to Gloryhole");
            }
            if (!myUser.PublishRequest(msg, "Gloryhole")) {
                System.err.println("[Client Error Message] :Something gone wrong, message not sent to Gloryhole");
            }
        }
        if (!myUser.SubscribeRequest("Gloryhole", "unsubscribe"))
            System.err.println("[Client Error Message] : Something gone wrong,retry to unsubscribe on Gloryhole topic");
        if (!myUser.SubscribeRequest("HelpCenter", "subscribe"))
            System.err.println("[Client Error Message] : Something gone wrong,retry to subscribe on HelpCenter topic");
        else{
            if (!myUser.PublishRequest(msg2, "HelpCenter")) {
                System.err.println("[Client Error Message] :Something gone wrong, message not sent to HelpCenter");
            }
        }

        myUser.PrintMap();
        if (!myUser.ConnectionRequest(args[0],myUser.username,myUser.password, "disconnect")) {
            System.err.println("[Client Error Message] : Mortino ,Something gone wrong,cannot disconnect from the server");
            System.exit(-1);
        }
    }
}