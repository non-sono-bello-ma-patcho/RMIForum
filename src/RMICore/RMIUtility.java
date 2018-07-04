package RMICore;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;

/* why won't you update? */
public class RMIUtility {
    private Registry ServerRegistry;
    private int serverPort;
    private String Salias;
    private  String Calias;

    public RMIUtility (int sp, String sa, String ca){
        serverPort = sp;
        Salias = sa;
        Calias = ca;
    }

    public int serverSetUp(Remote obj, String Localhost) {
        System.setProperty("java.rmi.server.hostname", Localhost);
        System.setProperty("java.security.policy", policyFile()); // define directory for windows...        if (System.getSecurityManager()==null) System.setSecurityManager(new SecurityManager());
        int res = -1;
        // RMIServer obj = new RMIServer();
        try {
            while(true){
                try{
                    serverPort=setRegistry(serverPort);
                    break;
                } catch (RemoteException e){
                    if ( obj instanceof RMIServerInterface ){
                        System.err.println("A server instance is already running");
                        System.exit(1);
                    }
                    serverPort++;
                }
            }
            res = ExportNBind(ServerRegistry, obj, Salias,serverPort);
            System.err.println((obj instanceof RMIServerInterface?"Server up and running on:"+Localhost:"Registry correctly set")); /* non va bene per il client*/
        } catch (AccessControlException e) {
            System.err.println("You must set policy in order to set registry!");
            // showStackTrace(e);
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Couldn't set registry, maybe you want to check stack trace?[S/n]");
            showStackTrace(e);
        } /*catch (AlreadyBoundException e) {
            System.err.println("Couldn't export and bind, on port "+ res +" maybe you want to check stack trace?[S/n]");
            // showStackTrace(e);
        }*/
        return res;
    }

    static void showStackTrace(Exception e){
        /*Scanner sc = new Scanner(System.in);
        if(sc.nextInt()!='n')*/ e.printStackTrace();
    }

    public Remote getRemoteMethod(String host, int port) throws RemoteException, NotBoundException {
        System.err.println("Trying to retrieve registry from"+host+"...");
        Registry registry = LocateRegistry.getRegistry(host, port);
        System.err.print("LookingUp for share Object: ");
        return registry.lookup(Calias);
    }

    public void RMIshutDown(Remote obj) throws RemoteException, NotBoundException {
        ServerRegistry.unbind(Salias);
        UnicastRemoteObject.unexportObject(obj, true);
    }

    private int setRegistry(int port) throws RemoteException {
        ServerRegistry = LocateRegistry.createRegistry(port);
        return port;
    }

    private int ExportNBind(Registry reg, Remote obj, String alias, int port) throws RemoteException {
        Remote stub;
            try {
                System.err.println("Exporting on port: "+port);
                stub = UnicastRemoteObject.exportObject(obj, port);
                reg.bind(alias, stub);
            } catch (ExportException e) {
                System.err.println(e.toString());
            } catch (AlreadyBoundException e) {
                System.err.println("This object is already bound");
            }
        return port;
    }

    private String policyFile() {
        File policy = null;
        try {
            policy = File.createTempFile("RMIServer", ".policy");
        // magari ci scriviamo sopra qualcosa va(?)
        PrintWriter pw = new PrintWriter(policy, "UTF-8");
        pw.println("\"grant {\n\tpermission java.security.AllPermission\n};");
        pw.close();
        return policy.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; //TODO: modify return....
    }

}


