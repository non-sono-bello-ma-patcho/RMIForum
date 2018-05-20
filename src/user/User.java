package user;
import core.*;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class User implements RMIClient{
    public Registry pullRegistry; /* Registry used for pulling remote method */
    public Registry pushRegistry; /* Registry used for pushing remote method */
    public RMIServer ServerConnected; /* that is the stub */
    private String usurname; /* we can also change the implementation and ask that parameter later */
    private String pswd; /* we can also change the implementation and ask that parameter later */
    private boolean connected = false;

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
                    ServerConnected = (RMIServer) pullRegistry.lookup("ToBeDecided");
                    /*remember to switch the lookup parameter with the right one */
                    boolean result = ServerConnected.ManageConnection(usurname,pswd,op);
                    if(result == true ) connected = true;
                    return result;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                if(connected == false){
                    System.err.println("You are already disconnected");
                    return true;
                }
                boolean result = ServerConnected.ManageConnection(usurname,pswd,op);
                if(result == true ) connected = false;
                return result;

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
                return ServerConnected.ManageSubscribe(TopicName,false); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            case "unsubscribe":
                return ServerConnected.ManageSubscribe(TopicName,true); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
            default:
                System.err.println("invalid operation");
        }
        return false; /*something gone wrong, probably wrong operation*/
        /*if the topic doesn't exist, maybe the server could create it and subscribe that user and than return (?)*/
    }


    private void MessageRequest(){

    }

    public void CLiNotify(){ /* should it be synchronized??? */

    }

    public String GetUsername(){
        return this.usurname;
    }

    public String GetPassword(){
        return this.pswd;
    }


    public static void main(String[] args){

    }
}
