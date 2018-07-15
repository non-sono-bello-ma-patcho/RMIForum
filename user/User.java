package RMIForum.user;


import RMIForum.RMICore.*;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


import static java.lang.Math.abs;


public class User implements RMIClient{
    //  public Registry pushRegistry; /* Registry used for pushing remote method */ INTERNO NELLA CLASSE RMIUtility, non viene mai chiamato...
    public RMIServerInterface ServerConnected;
    private RMIClient stub;
    private boolean connected = false;
    private final int serverPort = 1969;
    private String host = "none";
    private String username;
    private String password;
    private List<String> brokerID = null;
    private RMIUtility ClientHandler;
    private TopicList ServerTopics;
    private HashMap<String, List<MessageClass>> TopicsMessages;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public RMIServerInterface.ConnResponse Errorstatus = null;

    /*     constructor    */

    public User() throws UnknownHostException {
        ClientHandler = new RMIUtility(1099,"RMISharedClient","RMISharedServer");
        ServerTopics = new TopicList();
        TopicsMessages = new HashMap<>();
    }

    public User(List<String> b) throws UnknownHostException {
        ClientHandler = new RMIUtility(1099,"RMISharedClient","RMISharedServer");
        ServerTopics = new TopicList();
        TopicsMessages = new HashMap<>();
        brokerID = b;
    }


    /*    auxiliary functions   */
    public void CheckConnection(){
        if(!connected){
            printDebug("NotConnected");
            // System.exit(-1); //lo lascio, poichè l'errore della connessione viene gestito altrove. questo è un caso partcolare.
        }
    }

    private void exportStub(){
        int port = 1099;
        while(true) {
            try {
                stub = (RMIClient) UnicastRemoteObject.exportObject(this, port);
                break;
            } catch (RemoteException e) {
                port++;
            }
        }
    }

    private void ChargeData() throws RemoteException{
        CheckConnection();
        printDebug("Trying to fetch data from server.....");
        ServerTopics = ServerConnected.getTopics();
        for(String k : ServerTopics.ListTopicName()) {
            if (TopicsMessages.containsKey(k)) TopicsMessages.replace(k, ServerTopics.getTopicNamed(k).getConversation());
            else TopicsMessages.put(k, ServerTopics.getTopicNamed(k).getConversation());
        }
    }

    private void CheckError(){
        ServerConnected = null;
        try{
            UnicastRemoteObject.unexportObject(this , true);
        } catch(RemoteException e){
            printDebug("No object to unexport...");
        }
        printDebug("["+username+" Error Message]: "+Errorstatus.toString());
    }

    /*              Principal functions           */

    public  boolean ConnectionRequest(String Serverhost,String user) throws  RemoteException {
        if (connected){
            printDebug("You are already connected");
            return false;
        }

        printDebug("Trying to connect to the server " + Serverhost);
        username = user;
        try {
            exportStub();
            ServerConnected = (RMIServerInterface) ClientHandler.getRemoteMethod(Serverhost,serverPort);
            Errorstatus = ServerConnected.ManageConnection(user, stub, brokerID,"connect");
            if(Errorstatus.equals(RMIServerInterface.ConnResponse.Success)) {
                connected = true;
                ChargeData();
            } else  CheckError();
            return connected;
        }catch (NotBoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(){
        printDebug("Trying to disconnect from server...");
        CheckConnection();
        if(!connected) return false;
        try {
            Errorstatus = ServerConnected.ManageConnection(username, stub, brokerID, "disconnect");
            if(Errorstatus == RMIServerInterface.ConnResponse.Success) {
                connected = false;
                UnicastRemoteObject.unexportObject(this , false);
                return true;
            }else CheckError();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean SubscribeRequest(String TopicName, String op) throws RemoteException {
        CheckConnection();
        if(!connected) return false;
        switch(op){
            case "subscribe":
               printDebug("Trying to subscribe to : "+TopicName);
                return ServerConnected.ManageSubscribe(TopicName,username,false);
            case "unsubscribe":
                printDebug("Trying to unsubscribe to : "+TopicName);
                return ServerConnected.ManageSubscribe(TopicName,username,true);
            default:
                printDebug("Invalid operation");
        }
        return false;
    }

    public boolean AddTopicRequest(String TopicName) throws RemoteException {
        printDebug("Trying to add topic : "+TopicName);
        if(!connected) return false;
        CheckConnection();
        return ServerConnected.ManageAddTopic(TopicName, username);
    }

    public boolean PublishRequest(String text, String TopicName) throws RemoteException {
        printDebug("Trying to send message to: "+TopicName);
        CheckConnection();
        if(!connected) return false;
        return ServerConnected.ManagePublish(new MessageClass(username,text),TopicName);
    }

    @Override
    public  void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        CheckConnection();
        if (TopicLabel.equals("TestInvoke")) System.out.println("\t"+username+"SUCCESSFUL CONNECTION");
        else {
            // notifier.add(TopicLabel);
            if (type) System.out.println("_NP_" + " " + TriggeredBy + " " + TopicLabel);
            else System.out.println("_NT_" + " " + TriggeredBy + " " + TopicLabel);
            ChargeData();
        }
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

    public TopicList getServerTopics(){
        return ServerTopics;
    }

    public String getHost(){ return host;}


    /*      Debugging function     */
    private void PrintMap(){
        for(String k :  ServerTopics.ListTopicName()){
            System.out.println("[Debugging] : Topic = "+"["+k+"]");
            for(MessageClass m : TopicsMessages.get(k))
                System.out.println("                          "+"["+m.getUser()+"] : "+m.getText());
        }
    }

    private void printDebug(String s){
        System.err.println("[ClientDebug]: "+text);
    }

    private static class clientRequestConnection extends Thread{
        private int clinum;
        private String address;
        public clientRequestConnection(int cn, String a){
            clinum = cn;
            address = a;
        }
        @Override
        public void run(){
            try {
                User tempuser = new User();
                tempuser.ConnectionRequest(address, "client_"+clinum);
                //tempuser.SubscribeRequest("HelpCenter", "subscribe");
                if(tempuser.AddTopicRequest(tempuser.username+" Topic")){
                    System.err.println("Added");
                    tempuser.SubscribeRequest(tempuser.username+" Topic", "subscribe");
                    tempuser.PublishRequest(tempuser.GetUsername()+" Hello world!", tempuser.username+" Topic");
                }
                else {
                    System.err.println("Topic refused. Exit");
                }
                // tempuser.PublishRequest(tempuser.GetUsername()+" is not a program created by rollingflamingo....", "HelpCenter");
                sleep(abs(new Random().nextInt()%1000));
                System.err.println("\t\tDisconnection attempt for "+tempuser.GetUsername());
                tempuser.disconnect();
                System.err.println("\t\tDisconnect attempt for "+tempuser.GetUsername()+" successful");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException, RemoteException, InterruptedException {
        /*debug messages */

        /* end messages */
        // TODO: modify constructor so that the localhost is passed as a parameter (this works only on loopback...)
        User myUser = new User();

        myUser.PrintMap();


        User anotherUser = new User();
        if(anotherUser.ConnectionRequest(args[1], "andreo"))System.err.println("Connected");
        if(anotherUser.AddTopicRequest("HelpCenter")) System.err.println("Added");
        else {
            System.err.println("Topic refused. Exit");
            System.exit(0);
        }

        if(anotherUser.disconnect())System.err.println("Disconnected");

        System.out.println("Starting multi request:");

        clientRequestConnection threads[] = new clientRequestConnection[10];
        for(int i=0; i< threads.length; i++){
            threads[i] = new clientRequestConnection(i, args[0]);
            threads[i].start();
        }

        for(int i=0; i< threads.length; i++){
            threads[i].join(1000);
        }
        System.exit(0);
    }
}
