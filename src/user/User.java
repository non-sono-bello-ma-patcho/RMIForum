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


public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
    public Registry pushRegistry; /* Registry used for pushing remote method */
    public RMIServer ServerConnected; /* that is the stub */
    private String usurname;
    private String pswd;
    private boolean connected = false;
    private final int myListeningPort = 1968;
    private HashMap<String, TopicClass> myTopics;
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
                    if(result == true ) connected = true;
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
                return ServerConnected.ManageSubscribe(usurname,TopicName,false); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            case "unsubscribe":
                return ServerConnected.ManageSubscribe(usurname,TopicName,true); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            default:
                System.err.println("invalid operation");
        }
        return false; /*something gone wrong, probably wrong operation*/
        /*if the topic doesn't exist, maybe the server could create it and subscribe that user and than return (?)*/
    }


    private boolean MessageRequest(MessageClass msg,String topicName) throws RemoteException {
        if(connected == false){
            System.err.println("Permission denied! you are not connected!");
            return false;
        }
        ServerConnected.ManagePublish(msg,topicName);
        return true;
    }

    public void CLiNotify() throws RemoteException {
        if(connected == false){
            System.err.println("Permission denied! The client isn't connected");
            return;
        }
        myTopics = ServerConnected.getTopics();
        // if client subscribed, then notify...

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
