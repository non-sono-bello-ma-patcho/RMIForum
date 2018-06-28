package Test;

import Server.RMIServer;
import user.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static java.lang.Thread.sleep;

public class ServerTester extends RMIServer {
    private User clientSide;

    public ServerTester(String address) throws UnknownHostException {
        super(address);
        clientSide = new User(address);
    }

    /*-- Server Side --*/
    /*-- Make all this functions selective --*/
    @Override
    public ConnResponse ManageConnection(String username, String password, String address, int port, String op) throws RemoteException {
        return super.ManageConnection(username, password, address, port, op);
    }

    @Override
    public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException{
        return super.ManageAddTopic(TopicName, TopicOwner);
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

    public static void main(String [] args){
        try {
            ServerTester st = new ServerTester(InetAddress.getLocalHost().getHostAddress());
            st.start();
            sleep(10000);
            st.shutdown();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
