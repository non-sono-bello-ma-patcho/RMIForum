package core;

import java.io.Serializable;

public class MessageClass implements Serializable {
    private String Text;
    private String User;

    public MessageClass(String user, String text){
        Text = text;
        User = user;
    }

    public String getText(){
        return Text;
    }

    public String getUser(){
        return User;
    }

    public String getFormatMsg(){
        return  "[" + getUser()+"]: "+getText();
    }
}
