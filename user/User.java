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


    /*    auxiliary functions   */
    public void CheckConnection(){
        /*if(!connected){
            System.err.println("[Client Error Message] : NotConnected");
            System.exit(-1); //lo lascio, poichè l'errore della connessione viene gestito altrove. questo è un caso partcolare.
        }*/
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
        System.out.println(ANSI_BLUE+ "[Client Message] : Trying to fetching data from the server....."+ANSI_RESET);
        ServerTopics = ServerConnected.getTopics();
        for(String k : ServerTopics.ListTopicName()) {
            if (TopicsMessages.containsKey(k)) TopicsMessages.replace(k, ServerTopics.getTopicNamed(k).getConversation());
            else TopicsMessages.put(k, ServerTopics.getTopicNamed(k).getConversation());
        }
        System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
    }

    private void CheckError(){
        System.err.println("["+username+" Error Message]: "+Errorstatus.toString());
    }

    /*              Principal functions           */

    public  boolean ConnectionRequest(String Serverhost,String user,String psw) throws  RemoteException {
        if (connected){
            System.err.println("[Client Error Message] : You are already connected");
            return false;
        }

        System.out.println(ANSI_BLUE+"[Client Message] : Trying to connect to the server " + Serverhost + " ..."+ANSI_RESET);
        username = user;
        password = psw;
        try {
            exportStub();
            ServerConnected = (RMIServerInterface) ClientHandler.getRemoteMethod(Serverhost,serverPort);
            Errorstatus = ServerConnected.ManageConnection(user, stub, "connect");
            if(Errorstatus.equals(RMIServerInterface.ConnResponse.Success)) {
                connected = true;
                System.out.println(ANSI_BLUE+"[Client Message] : Done."+ANSI_RESET);
                ChargeData();
            } else  CheckError();
            return connected;
        }catch (NotBoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(){
        System.out.println(ANSI_BLUE+"[Client Message] : Trying to disconnect from the server..."+ANSI_RESET);
        CheckConnection();
        try {
            Errorstatus = ServerConnected.ManageConnection(username, stub, "disconnect");
            if(Errorstatus == RMIServerInterface.ConnResponse.Success) {
                connected = false;
                UnicastRemoteObject.unexportObject(this , false);
                System.out.println(ANSI_BLUE + "["+username+" Message] : Done." + ANSI_RESET);
                return true;
            }else CheckError();
        } catch (RemoteException e) {
            e.printStackTrace();
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
                System.err.println("["+username+" Error Message] : invalid operation");
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
                tempuser.ConnectionRequest(address, "client_"+clinum, "1234");
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
        if(anotherUser.ConnectionRequest(args[1], "andreo", "1234"))System.err.println("Connected");
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
