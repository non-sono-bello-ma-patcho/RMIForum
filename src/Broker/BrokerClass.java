package Broker;

import RMICore.TopicList;
import Server.RMIServer;
import user.User;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class BrokerClass extends User {
    private RMIServer ServerSide;

    public BrokerClass() throws UnknownHostException {
        super();
        ServerSide = new RMIServer();
    }

    /*-- Server Side --*/
    /*-- Make all this functions selective --*/



    /*
    @Override
    public static void printInfo(RMIServer rs){  //static void generates problems with overriding
      super.printInfo(rs);
    }
    */


    public void start(String address){
        ServerSide.start(address);
    }

    public void shutdown(){
        try {
            super.disconnect();
            ServerSide.shutDown();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void CLiNotify(String TopicLabel,String TriggeredBy, boolean type) throws RemoteException {
        super.CLiNotify(TopicLabel, TriggeredBy, type);
        ServerSide.Notify(TopicLabel, TriggeredBy, type);
    }

    /* Server Methods: */

    public boolean kickUser(String user){
        return ServerSide.kickUser(user);
    }

    public boolean removeTopic(String topicLabel){
        return ServerSide.removeTopic(topicLabel);
    }

    public boolean removeMessage(String topicLabel, String message){
        return ServerSide.removeMessage(topicLabel, message);
    }
        // GETTERS //




    /*non ha senso */


    public static void main(String[] args){


    }
}
