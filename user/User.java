package RMIForum.user;


import RMIForum.RMICore.*;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;


import static java.lang.Math.abs;


public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
    //  public Registry pushRegistry; /* Registry used for pushing remote method */ INTERNO NELLA CLASSE RMIUtility, non viene mai chiamato...
    public RMIServerInterface ServerConnected;
    private boolean connected = false;
    private int myListeningPort;
    private final int serverPort = 1969;
    private String host;
    private String username;
    private String password;
    private RMIUtility ClientHandler;
    private TopicList ServerTopics;
    private HashMap<String, List<MessageClass>> TopicsMessages;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public RMIServerInterface.ConnResponse Errorstatus = null;

    /*     constructor    */

    public User(String myHost) throws UnknownHostException {
        host = myHost;
        ClientHandler = new RMIUtility(1099,"RMISharedClient","RMISharedServer");
        ServerTopics = new TopicList();
        TopicsMessages = new HashMap<>();
    }


    /*    auxiliary functions   */
    public void CheckConnection(){
        if(!connected){
            System.err.println("[Client Error Message] : NotConnected");
            System.exit(-1); //lo lascio, poichè l'errore della connessione viene gestito altrove. questo è un caso partcolare.
        }
    }

    private void ChargeData() throws RemoteException{
        CheckConnection();
        System.out.println(ANSI_BLUE+ "[Client Message] : Trying to fetching data from the server....."+ANSI_RESET);
        ServerTopics = ServerConnected.getTopics();
        for(String k : ServerTopics.ListTopicName()) {
            if (TopicsMessages.containsKey(k)) TopicsMessages.replace(k, ServerTopics.getTopicNamed(k).getConversation());
            else TopicsMessages.put(k, ServerTopics.getTopicNamed(k).getConversation());
        }
        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
    }

    private void CheckError(){
        System.err.println("[Client Error Message] : "+Errorstatus.toString());
    }

    /*              Principal functions           */

    public  boolean ConnectionRequest(String Serverhost,String user,String psw, String op) throws  RemoteException {
        switch(op){
            case "connect":
                if (connected){
                    System.err.println("[Client Error Message] : You are already connected");
                    return false;
                }
                myListeningPort = ClientHandler.serverSetUp(this, host);
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to connect to the server " + Serverhost + " ..."+ANSI_RESET);
                username = user;
                password = psw;
                try {
                    ServerConnected = (RMIServerInterface) ClientHandler.getRemoteMethod(Serverhost,serverPort);
                    Errorstatus = ServerConnected.ManageConnection(user, psw, this.host, myListeningPort, op);
                    if(Errorstatus.equals(RMIServerInterface.ConnResponse.Success)) {
                        connected = true;
                        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
                        ChargeData();
                    } else  CheckError();
                    return connected;
                }catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                System.out.println(ANSI_BLUE+"[Client Message] : Trying to disconnect from the server..."+ANSI_RESET);
                CheckConnection();
                try {
                    System.err.println("Sending disconnection request");
                    Errorstatus = ServerConnected.ManageConnection(username, password, this.host, myListeningPort, op);
                    System.err.println("DONE");
                    if(Errorstatus.equals(RMIServerInterface.ConnResponse.Success)) {
                        connected = false;
                        ClientHandler.RMIshutDown(this);
                        System.out.println(ANSI_BLUE + "["+username+" Message] : Done." + ANSI_RESET);
                        return true;
                    }else CheckError();
                }catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.err.println("[Client Error Message] : invalid operation");
        }
        return false;
    }


    public boolean SubscribeRequest(String TopicName, String op) throws RemoteException {
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

    public boolean AddTopicRequest(String TopicName) throws RemoteException {
        System.out.println(ANSI_BLUE+"[Client Message] : Trying to add the topic : "+TopicName+"..."+ANSI_RESET);
        CheckConnection();
        return ServerConnected.ManageAddTopic(TopicName, username);
    }

    public boolean PublishRequest(String text, String TopicName) throws RemoteException {
        System.out.println(ANSI_BLUE+"[Client Message] : Trying to send the message on : "+TopicName+"..."+ANSI_RESET);
        CheckConnection();
        ServerConnected.ManagePublish(new MessageClass(username,text),TopicName);
        return true;
    }

    @Override
    public  void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
        CheckConnection();
        // notifier.add(TopicLabel);
        if(type) System.out.println("_NP_"+" "+TriggeredBy+" "+TopicLabel);
        else System.out.println("_NT_"+" "+TriggeredBy+" "+TopicLabel);
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

    public TopicList getServerTopics(){
        return ServerTopics;
    }

    /*      Debugging function     */
    private void PrintMap(){
        for(String k :  ServerTopics.ListTopicName()){
            System.out.println("[Debugging] : Topic = "+"["+k+"]");
            for(MessageClass m : TopicsMessages.get(k))
                System.out.println("                          "+"["+m.getUser()+"] : "+m.getText());
        }
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
                User tempuser = new User(address);
                tempuser.ConnectionRequest(address, "client_"+clinum, "1234", "connect");
                tempuser.SubscribeRequest("HelpCenter", "subscribe");
                tempuser.PublishRequest(tempuser.GetUsername()+" is not a program created by rollingflamingo....", "HelpCenter");
                sleep(abs(new Random().nextInt()%1000));
                tempuser.ConnectionRequest(address, "client_"+clinum, "1234", "disconnect");
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
        User myUser = new User(args[0]);

        myUser.PrintMap();


        User anotherUser = new User(args[0]);
        if(anotherUser.ConnectionRequest(args[1], "andreo", "1234", "connect"))System.err.println("Connected");
        if(anotherUser.ConnectionRequest(args[1], "andreo", "1234", "disconnect"))System.err.println("Disconnected");

        System.out.println("Starting multi request:");

        clientRequestConnection threads[] = new clientRequestConnection[10];
        for(int i=0; i< threads.length; i++){
            threads[i] = new clientRequestConnection(i, args[0]);
            threads[i].start();
        }

        for(int i=0; i< threads.length; i++){
            threads[i].join();
        }
        // System.exit(0);
    }
}