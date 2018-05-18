package user;
import core.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class User implements RMIClient{
    Registry pullRegistry; /* Registry used for pulling remote method */
    Registry pushRegistry; /* Registry used for pushing remote method */
    RMIServer ServerConnected; /* that is the stub */
    String usurname; /* we can also change the implementation and ask that parameter later */
    String pswd; /* we can also change the implementation and ask that parameter later */

    /*maybe there is a better way to initialize psw and usurname */
    public User(String nick, String password, int port){
        usurname = nick;
        pswd = password;
        try {
            pushRegistry = LocateRegistry.createRegistry(port); /* to be modified later... move that raw in the main function after the connection */
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /*method that try to connect/disconnect to the rmi server */
    private  boolean ConnectionRequest(String host, String op){
        switch(op){
            case "connect":
                try {
                    pullRegistry = LocateRegistry.getRegistry(host);
                    ServerConnected = (RMIServer) pullRegistry.lookup("ToBeDecided");
                    /*remember to switch the lookup parameter with the right one */
                    return ServerConnected.ManageConnection(usurname,pswd,op);

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;

            case "disconnect":
                if(pullRegistry == null){
                    System.err.println("You are already disconnected");
                    return true;
                }
                return ServerConnected.ManageConnection(usurname,pswd,op);

            default:
                System.err.println("invalid operation");
                break;
        }
        return false;
    }

/* that method is for the topic registration... there is no definition (for now) for a server registration request */
    private boolean SubscribeRequest(String TopicName){ /* how could the user choose the Topic he wants to subscribe in? */  /*--> updated*/
        return ServerConnected.ManageSubscribe(TopicName); /* assuming that the server class will use an hash map <String,Topic> where the string is the label*/
        /*if the topic doesn't exist, maybe the server culd create it and subscribe that user and than return (?)*/
    }


    private void MessageRequest(){

    }

    public void CLiNotify(){ /* should it be synchronized??? */

    }

    public static void main(String[] args){

    }
}
