package RMIForum.Broker;

import RMIForum.RMICore.MessageClass;
import RMIForum.RMICore.RMIClient;
import RMIForum.RMICore.TopicClass;
import RMIForum.RMICore.TopicList;
import RMIForum.Server.RMIServer;
import RMIForum.user.User;

import java.util.ArrayList;
import java.util.List;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BrokerClass extends RMIServer {
        private class BrokerUser extends User{
            public BrokerUser(){
                super();
            }

            public BrokerUser(List<String> b){
                super(b);
            }

            @Override
            public void CLiNotify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
                super.CLiNotify(TopicLabel, TriggeredBy, type);
                Notify(TopicLabel, TriggeredBy, type);
            }
        }

        BrokerUser ClientSide;

        public BrokerClass() throws UnknownHostException {
            super();
            List <String> l = getChildrenIDs();
            l.add(UUID.randomUUID().toString());
            ClientSide = new BrokerUser(l);
        }

        /*-- Server Side --*/
        /*-- Make all this functions selective --*/
        @Override
        public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
            // verifico che il topic non sia su un altro broker...
            if(!getTopics().contains(TopicName)){
                // cerco il topic sull'altro broker e all'occorrenza chiamo ManagePublish su quello...
                if(!ClientSide.GetConnectonStatus()) return false;
                return ClientSide.ServerConnected.ManagePublish(msg, TopicName);
            }
            if (!getTopics().getTopicNamed(TopicName).hasUser(msg.getUser())) return false;
            printDebug("Publishing |" + msg.getFormatMsg() + "| to [" + TopicName + "]!");
            (getTopics().getTopicNamed(TopicName)).addMessage(msg);
            Notify(TopicName, msg.getUser(), true); // update local users convos...
            return true;
        }

        @Override
        public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
            printDebug("["+User+"] wants to "+(!unsubscribe?"subscribe to ":"unsubscribe from ")+" ["+TopicLabel+"]: ");
            if(!getTopics().contains(TopicLabel)){
                printDebug("No such topic...");
                if(!ClientSide.GetConnectonStatus())return false;
                else return ClientSide.ServerConnected.ManageSubscribe(TopicLabel, User, unsubscribe);
            }
            if(!unsubscribe) return (getTopics().getTopicNamed(TopicLabel)).addUser(User);
            else return getTopics().getTopicNamed(TopicLabel).RemoveUser(User);
        }

        @Override
        public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
            boolean hasu = false;
            List<Future<String>> response = new ArrayList<>(ClientList.size());
            for (String s : ClientList.keySet()) {
                try {
                    hasu = ClientList.get(s).getMyTopic().contains(TopicLabel);
                    if (hasu || !type) { // notify only if a topic has been added or the user is subscribed...
                        printDebug("Notifying [" + s + "]:");
                        response.add(notifyClient(s, ClientList.get(s), TopicLabel, TriggeredBy, type));
                    }
                } catch (RemoteException re){
                    printDebug("Impossible to invoke CliNotify from " + s + ": removing it from clients:");
                    kickUser(s);
                }
            }
            for (Future<String> f : response) {
                String result = null;
                try {
                    result = f.get();
                } catch (InterruptedException e) {
                    e.printStackTrace(); // TODO: Handle exception
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    if (!result.equals("Reached")) {
                        printDebug("Impossible to invoke CliNotify from " + result + ": removing it from clients:");
                        kickUser(result);
                    }
                } catch (NullPointerException npe){
                    System.err.println("no result found...");
                }
            }
        }


        /* Client Side */

        public  boolean ConnectionRequest(String Serverhost,String user) throws  RemoteException {
            return ClientSide.ConnectionRequest(Serverhost,user);
        }

        public boolean disconnect(){
            return ClientSide.disconnect();
        }

        public boolean SubscribeRequest(String TopicName, String op) throws RemoteException {
            return ClientSide.SubscribeRequest(TopicName,op);
        }

        public boolean AddTopicRequest(String TopicName) throws RemoteException {
            return ClientSide.AddTopicRequest(TopicName);
        }

        public boolean PublishRequest(String text, String TopicName) throws RemoteException {
            return ClientSide.PublishRequest(text, TopicName);
        }

        public User getBrokerUser(){
            return ClientSide;
        }

        public TopicList getServerTopics() throws RemoteException {
            return getTopics();
        }

}
