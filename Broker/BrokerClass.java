package RMIForum.Broker;

import RMIForum.RMICore.TopicList;
import RMIForum.Server.RMIServer;
import RMIForum.user.User;
import parser.ast.Unsubscribe;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BrokerClass extends RMIServer {
    private User clientSide;

    public BrokerClass() throws UnknownHostException {
        super();
        clientSide = new User();
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
        super.start(address);
    }

    public void shutdown(){
        try {
            super.shutDown();
            clientSide.disconnect();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    // client methods side //
    public boolean ConnectionRequest(String address, String username) throws RemoteException {
        return clientSide.ConnectionRequest(address,username);
    }

    public boolean Disconnect(){
        return clientSide.disconnect();
    }
    /*
        public boolean SubscribeRequest(String TopicName,String operation) throws RemoteException{
            return clientSide.SubscribeRequest(TopicName,operation);
        }
    */
    public boolean AddTopicRequest(String Topicname) throws RemoteException{
        return clientSide.AddTopicRequest(Topicname);
    }

    public boolean PublishRequest(String text, String Topicname) throws RemoteException {
        return clientSide.PublishRequest(text,Topicname);
    }

    public void CliNotify(String TopicLabel,String TriggeredBy, boolean type) throws RemoteException {
        clientSide.CLiNotify(TopicLabel,TriggeredBy,type);
    }

    public boolean Subscribe(String topicLabel) throws RemoteException {
        return clientSide.SubscribeRequest(topicLabel, "subscribe");
    }

    public boolean Unsubscribe(String topicLabel) throws RemoteException {
        return clientSide.SubscribeRequest(topicLabel, "unsubscribe");
    }

    // GETTERS //

    public String GetUsername(){
        return clientSide.GetUsername();
    }

    public String GetPassword(){ return clientSide.GetPassword();}

    public boolean GetConnectonStatus(){
        return clientSide.GetConnectonStatus();
    }

    public TopicList getServerTopics(){
        return clientSide.getServerTopics();
    }
    
    public List<String> getConnectedUsers(){
        return new ArrayList<String>(ClientList.keySet());
    }

    public String getHost(){ return clientSide.getHost();}

    public User getUser(){ return clientSide;}




    /*non ha senso */


    public static void main(String[] args){


    }
}
