package Test;

import Server.RMIServer;
import user.User;

import java.net.UnknownHostException;

public class ServerTester extends RMIServer {
    private User clientSide;

    public ServerTester(String address) throws UnknownHostException {
        super(address);
        clientSide = new User(address);
    }

    public void start(){
        start();
    }
}
