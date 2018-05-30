package core;

import Server.RMIServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;
import java.util.Scanner;

public class RMIUtility {
    private Registry ServerRegistry;
    private int serverPort;
    private int clientPort;
    private String Salias;
    private  String Calias;

    public RMIUtility(Registry reg, int sp, int cp, String sa, String ca){
        ServerRegistry = reg;
        clientPort = cp;
        serverPort = sp;
        Salias = sa;
        Calias = ca;
    }

    public void serverSetUp(Remote obj, String Localhost) {
        System.setProperty("java.rmi.server.hostname", Localhost);
        System.setProperty("java.security.policy", "/tmp/RMIServer.policy");
        if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        // RMIServer obj = new RMIServer();
        try {
            ServerRegistry=setRegistry(serverPort);
            ExportNBind(ServerRegistry, obj, Salias,serverPort);

            System.err.println((obj instanceof RMIServer?"Server up and running on:"+Localhost:"Registry correctly set")+", type something to shutdown..."); /* non va bene per il client*/

        } catch (AccessControlException e) {
            System.err.println("You must set policy in order to set registry!");
            showStackTrace(e);
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Couldn't set registry, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (AlreadyBoundException e) {
            System.err.println("Couldn't export and bind, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        }
    }

    static void showStackTrace(Exception e){
        /*Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n')*/ e.printStackTrace();
    }

    public Remote getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from"+host+"...");
        Registry registry = LocateRegistry.getRegistry(host, clientPort);
        System.err.print("LookingUp for share Object: ");
        return registry.lookup(Calias);
    }

    private Registry setRegistry(int port) throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(port);
        }
    }

    private void ExportNBind(Registry reg, Remote obj, String alias, int port) throws AlreadyBoundException, RemoteException {
        Remote stub;
        if(obj instanceof RMIServer) stub = (RMIServerInterface) UnicastRemoteObject.exportObject(obj, port);
        else stub = (RMIClient) UnicastRemoteObject.exportObject(obj, port);
        reg.bind(alias, stub);
    }

    public void RMIshutDown(Remote obj) throws RemoteException, NotBoundException {
        ServerRegistry.unbind(Salias);
        UnicastRemoteObject.unexportObject(obj, true);
    }

}
