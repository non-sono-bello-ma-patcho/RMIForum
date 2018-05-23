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

    public void serverSetUp(Object obj) throws UnknownHostException {
        //System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        System.setProperty("java.security.policy", "/tmp/RMIServer.policy");
        //if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        // RMIServer obj = new RMIServer();
        try {
            ServerRegistry=setRegistry(serverPort);
            ExportNBind(ServerRegistry, obj, Salias,serverPort);

            InetAddress ia = InetAddress.getLocalHost();
            System.err.println("Server up and running on:"+ia.getHostAddress()+", type something to shutdown...");
            Scanner sc = new Scanner(System.in);
            System.err.println("You typed: "+sc.next());
            RMIshutDown(obj);
        } catch (RemoteException e) {
            System.err.println("Couldn't set registry, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (AlreadyBoundException e) {
            System.err.println("Couldn't export and bind, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (UnknownHostException e) {
            System.err.println("Couldn't get localhost, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } catch (NotBoundException e) {
            System.err.println("Couldn't unbound, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);        }
    }

    static void showStackTrace(Exception e){
        /*Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n')*/ e.printStackTrace();
    }

    public RMIClient getRemoteMethod(String host) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from"+host+"...");
        Registry registry = LocateRegistry.getRegistry(host, clientPort);
        System.err.print("LookingUp for share Object: ");
        return (RMIClient) registry.lookup(Calias);
    }

    private Registry setRegistry(int port) throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(port);
        }
    }

    private void ExportNBind(Registry reg, Object obj, String alias, int port) throws AlreadyBoundException, RemoteException {
        RMIServerInterface stub = (RMIServerInterface) UnicastRemoteObject.exportObject((Remote) obj, port);
        reg.bind(alias, stub);
    }

    public void RMIshutDown(Object obj) throws RemoteException, NotBoundException {
        ServerRegistry.unbind("RMISharedServer");
        UnicastRemoteObject.unexportObject((Remote) obj, true);
    }

}
