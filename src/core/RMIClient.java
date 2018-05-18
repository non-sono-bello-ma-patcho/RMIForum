package core;

public interface RMIClient {
    void ConnectionRequest();
    void SubscribeRequest();
    void MessageRequest();
    void CLiNotify();
}
