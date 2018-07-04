package RMICore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

public class TopicList implements Serializable {
    private CopyOnWriteArrayList<TopicClass> topicList;

    public TopicList(){
        topicList = new CopyOnWriteArrayList<>();
    }

    /**
     *
     * @return list of registered topics.
     */
    public List<String> ListTopicName(){
        List<String> res = new ArrayList<>();
        for (TopicClass t : topicList){
            res.add(t.getName());
        }
        return res;
    }

    /**
     *
     * @param topic: named of the searched topic
     * @return
     */
    public boolean contains(String topic){
        return ListTopicName().contains(topic);
    }

    public List<MessageClass> getConversation(String topicname){
        return getTopicNamed(topicname).getConversation();
    }

    public TopicClass getTopicNamed(String topicName) throws NoSuchElementException{
        if(!contains(topicName)) throw new NoSuchElementException();
        for(TopicClass t : topicList) if(t.getName().equals(topicName)) return t;
        return null;
    }

    public Boolean put(TopicClass tc){
        return topicList.add(tc);
    }

    public Boolean remove(String TopicName){
        for(TopicClass t : topicList)
            if(t.getName().equals(TopicName)){
                topicList.remove(t);
                return true;
            }
        return false;
    }

}