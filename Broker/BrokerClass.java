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

    public class BrokerClass extends RMIServer {
        private User ClientSide;

        public BrokerClass() throws UnknownHostException {
            super();
            List <String> l = getChildrenIDs();
            l.add(UUID.randomUUID().toString());
            ClientSide = new User(l);
        }

        /*-- Server Side --*/
        /*-- Make all this functions selective --*/
        @Override
        public void start(String Host){
            super.start(Host);
        }

        @Override
        public ConnResponse ManageConnection(String username, RMIClient stub, List<String>BrokerID, String op) throws RemoteException {
            return super.ManageConnection(username,stub,BrokerID,op);
        }

        @Override
        public boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe) throws RemoteException {
            return super.ManageSubscribe(TopicLabel,User,unsubscribe);
        }


        @Override
        public boolean ManagePublish(MessageClass msg, String TopicName) throws RemoteException {
            return super.ManagePublish(msg,TopicName);
        }

        @Override
        public boolean ManageAddTopic(String TopicName, String TopicOwner) throws RemoteException {
            return super.ManageAddTopic(TopicName,TopicOwner);
        }

        @Override
        public boolean removeMessage(String TopicLabel, String message){
            return super.removeMessage(TopicLabel,message);
        }

        @Override
        public boolean ManualkickUser(String user) throws ExecutionException, InterruptedException {
            return super.ManualkickUser(user);
        }

        @Override
        public boolean removeTopic(String TopicLabel){
            return super.removeTopic(TopicLabel);
        }

        @Override
        public void Notify(String TopicLabel, String TriggeredBy, boolean type) throws RemoteException {
            super.Notify(TopicLabel,TriggeredBy,type);
        }

        @Override
        public TopicList getTopics() throws RemoteException {
            return super.getTopics();
        }

        @Override
        public List<String> getConnectedUsers() {
            return super.getConnectedUsers();
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
