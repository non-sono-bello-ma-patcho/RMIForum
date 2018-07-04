package Test;

import RMICore.TopicList;
import Server.RMIServer;
import user.User;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ServerTester extends RMIServer {
    private User clientSide;

    public ServerTester(String address) throws UnknownHostException {
        super(address);
        clientSide = new User(address);
    }

    /*-- Server Side --*/
    /*-- Make all this functions selective --*/



    /*
    @Override
    public static void printInfo(RMIServer rs){  //static void generates problems with overriding
      super.printInfo(rs);
    }
    */


    public void start(){
        super.start();
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
    public boolean ConnectionRequest(String username, String password) throws RemoteException {
        return clientSide.ConnectionRequest(getHost(),username,password);
    }

    public boolean Disconnect(){
        return clientSide.disconnect();
    }

    public boolean SubscribeRequest(String TopicName,String operation) throws RemoteException{
        return clientSide.SubscribeRequest(TopicName,operation);
    }

    public boolean AddTopicRequest(String Topicname) throws RemoteException{
        return clientSide.AddTopicRequest(Topicname);
    }

    public boolean PublishRequest(String text, String Topicname) throws RemoteException {
        return clientSide.PublishRequest(text,Topicname);
    }

    public void CliNotify(String TopicLabel,String TriggeredBy, boolean type) throws RemoteException {
        clientSide.CLiNotify(TopicLabel,TriggeredBy,type);
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

    public String getHost(){ return clientSide.getHost();}

    public User getUser(){ return clientSide;}




    /*non ha senso */


    public static void main(String[] args){


    }
}
