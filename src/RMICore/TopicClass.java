package RMICore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TopicClass implements Serializable {
    private List<MessageClass> Convo;
    private List<String> Users;
    private String Name, Owner;

    public TopicClass(String name, String owner){
        Name = name;
        Owner = owner;
        Convo = new ArrayList<>();
        Users = new ArrayList<>();
    }

    public String getOwner(){
        return Owner;
    }
    
    public boolean addUser(String username){
        return Users.add(username);
    }
    
    public boolean RemoveUser(String username) { return Users.remove(username); }
    public boolean removeMessage(String msg){
        for (MessageClass mc : Convo ){
            if(mc.getFormatMsg().equals(msg)) Convo.remove(mc);
            return true;
        }
        return false;
    }

    public boolean addMessage(MessageClass msg){
        return Convo.add(msg);
    }

    public String getName(){
        return Name;
    }

    public List<String> ListUsers(){
        return Users;
    }

    public List<MessageClass> getConversation(){return Convo;} /*ho cambiato la mia struttura*/

    public boolean hasUser(String user){
        for(String s : ListUsers()){
            if(s.equals(user)) return true;
        }
        return false;
    }

    public List<String> ListMessages(){
        List<String> tmp = new ArrayList<>();
        for(MessageClass m : Convo){
            tmp.add(m.getFormatMsg());
        }
        return tmp;
    }
}
