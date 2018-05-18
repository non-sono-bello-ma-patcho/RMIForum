package core;

public interface RMIServer {
    void ManageConnection();
    void ManageSubscribe();
    void Notify();
    void ManagePublish();
}
