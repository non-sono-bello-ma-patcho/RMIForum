package core;

import java.util.ArrayList;
import java.util.List;

public class TopicClass {
    private List<MessageClass> Convo;
    private List<String> Users;
    private String Name, Owner;

    public TopicClass(String name, String owner){
        Name = name;
        Owner = owner;
        Convo = new ArrayList<>();
        Users = new ArrayList<>();
    }

    public boolean addUser(String username){
        return Users.add(username);
    }
    public boolean RemoveUser(String username) { return Users.remove(username); }

    public boolean addMessage(MessageClass msg){
        return Convo.add(msg);
    }

    public String getName(){
        return Name;
    }

    public List<String> ListUsers(){
        return Users;
    }

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
