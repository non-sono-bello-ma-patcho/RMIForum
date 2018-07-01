package Test;

import RMICore.MessageClass;
import RMICore.TopicList;
import Server.RMIServer;
import user.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

public class ServerTester extends RMIServer {
    private User clientSide;
    private ConcurrentHashMap<String,Integer> SelectTopic;

    public ServerTester(String address) throws UnknownHostException {
        super(address);
        clientSide = new User(address);
        SelectTopic = new ConcurrentHashMap<>();
    }

    /*-- Server Side --*/
    /*-- Make all this functions selective --*/
    @Override
    public ConnResponse ManageConnection(String username, String password, String address, int port, String op) throws RemoteException {
        return super.ManageConnection(username,password,address,port,op);
    }


    public void ManageMap(){
        for(String k :  SelectTopic.keySet()) {
            while (true) {
                System.err.println("someone is trying to create new topic : " + k + " Accept? [Y/n]: ");
                Scanner sc = new Scanner(System.in);
                String op = sc.next();
                boolean breakLoop = false;
                switch (op) {
                    case "y":
                    case "Y":
                        SelectTopic.replace(k, 1);
                        breakLoop = true;
                        break;
                    case "n":
                    case "N":
                        SelectTopic.replace(k, -1);
                        breakLoop = true;
                        break;
                    default:
                        System.err.println("Invalid operation");
                }
                if(breakLoop) break;
            }
        }
    }

    @Override
    public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException{
        SelectTopic.put(TopicName,0);
        while(SelectTopic.get(TopicName).equals(0)){} // LOOK MANAGEMAP!
        if(SelectTopic.get(TopicName).equals(1)) {
            SelectTopic.remove(SelectTopic.get(TopicName)); //dovrei trovare un modo per non farlo andare in loop
            return super.ManageAddTopic(TopicName, TopicOwner);
        }
        else return false;
    }

    @Override
    public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException{
        return super.ManagePublish(msg,TopicName);
    }

    @Override
    public TopicList getTopics() throws RemoteException{
        return super.getTopics();
    }

    /*
    @Override
    public static void printInfo(RMIServer rs){  //static void generates problems with overriding
      super.printInfo(rs);
    }
    */
    @Override
    public boolean kickUser(String user){
        return super.kickUser(user);
    }

    @Override
    public boolean removeTopic(String TopicLabel){
        return super.removeTopic(TopicLabel);
    }


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
    public void test(ServerTester st) throws InterruptedException { // va in loop èerchè elimino solo nella manage add topic quindi
        //dipende dalla schedulazione
        while(!SelectTopic.isEmpty()) {
            sleep(1000);
            st.ManageMap();
        }
    }

    public static void main(String[] args){
        try {
            ServerTester st = new ServerTester(InetAddress.getLocalHost().getHostAddress());
            st.start();
            sleep(10000);
            st.test(st); // non ha senso va in loop perchè elimino solamente nella manage add topic
            st.shutdown();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
