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
    public RMIServerInterface ServerConnected;
    public core.RMIClient Stub;
    private boolean connected = false;
    private final int myListeningPort = 1099;
    private final int serverPort = 1969;
    private String host;
    private String username;
    private String password;
    private RMIUtility ClientHandler;
    private HashMap<String, TopicClass> ServerTopics;
    private HashMap<String, List<String>> TopicsMessages;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";


    /*     constructor    */

    public User(String myHost) throws UnknownHostException {
        host = myHost;
        ClientHandler = new RMIUtility(pushRegistry,myListeningPort,serverPort,"RMISharedClient","RMISharedServer");
        ServerTopics = new HashMap<>();
        TopicsMessages = new HashMap<>();
        ClientHandler.serverSetUp(this, host);
    }


    /*    auxiliary functions   */

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
            if (TopicsMessages.containsKey(k)) TopicsMessages.replace(k, ServerTopics.get(k).ListMessages());
            else TopicsMessages.put(k, ServerTopics.get(k).ListMessages());
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
                    connected = ServerConnected.ManageConnection(username, password, this.host, op);
                    if (!connected){
                        ClientHandler.RMIshutDown(this);
                        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
                    }
                    return connected;
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
        return ServerConnected.addTopic(TopicName, username);
    }

    private boolean MessageRequest(MessageClass msg,String TopicName) throws RemoteException {
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



        /****************************************************************************************************************
        ServerTopics = ServerConnected.getTopics();
        if(ServerTopics.size() == 0){
            System.out.println(ANSI_GREEN+"[Server Message] : Welcome to Flamingorum!" + ANSI_RESET);
            return;
        }

        Set<String> setTopic = ServerTopics.keySet();
        Iterator<String> myIterator = setTopic.iterator();
        while(myIterator.hasNext()){


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
            ************************************************************************************************************/


    }


    /*          getters       */
    public String GetUsername(){
        return this.username;
    }

    public String GetPassword(){
        return this.password;
    }

    public boolean GetConnectonStatus(){
        return this.connected;
    }




    public static void main(String[] args) {
        User myUser = null;
        try {
            myUser = new User("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            if (!myUser.ConnectionRequest(args[0],"Mortino","12345", "connect")) {
                System.err.println("[Client Error Message] : Mortino,Something gone wrong,retry to connect");
                System.exit(-1);
            }
            if (!myUser.AddTopicRequest("Gloryhole"))
                System.err.println("[Client Error Message] :The topic Gloryhole already exist");


            if (!myUser.SubscribeRequest("Gloryhole", "subscribe"))
                System.err.println("[Client Error Message] : Something gone wrong,retry to subscribe on Gloryhole topic");
            else {
                MessageClass myMessage = new MessageClass("Mortino", "Sarebbe bello vedere i piedi di Re Julien da quel buchino!");
                if (!myUser.MessageRequest(myMessage, "Gloryhole")) {
                    System.err.println("[Client Error Message] :Something gone wrong, message not sent to Gloryhole");

                }
            }
            if (!myUser.ConnectionRequest(args[0],myUser.username,myUser.password, "disconnect")) {
                System.err.println("[Client Error Message] : Mortino ,Something gone wrong,cannot disconnect from the server");
                System.exit(-1); /* that means that the server returned false*/
            }

        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}