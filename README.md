# RMIForum
Simple Java based Forum

This is a Work in progress, stay tuned

**This is the testing branch! If you need a stable release to import in your project checkout [PackageReady](https://github.com/non-sono-bello-ma-patcho/RMIForum/tree/1c86096bf1f2f65b2131ff7583ff8b8fa02f8e25)'s branch!!**

## RMIServer API
### Exportable methods:
```java
boolean ManageConnection(String username, String pw, String adress, int port, String op);
boolean ManageAddTopic(String TopicLabel, String TopicOwner);
boolean ManageSubscribe(String TopicLabel, String User, boolean unsubscribe);
boolean ManagePublish(MessageClass msg, String TopicLabel);
```
### Local methods:
```java
void Notify(String TopicLabel, String TriggeredBy, boolean type);
void start();
void shutDown();
void printInfo();
```
## RMIClient API
### Exportable methods:
```java
void CliNotify(String TopicLabel, String TriggeredBy, boolean type);
```
### Local methods:
```java
boolean ConnectionRequest(String SererverHost, String User, String pw, String op);
boolean AddTopicRequest(String TopicLabel);
boolean SubscribeRequest(String TopicLabel, String op);
boolean PublishRequest(MessageClass msg, String TopicLabel);
```
## Appearance
![Login Form](img/flamingorumIntro.png)
![Dash Form](img/flamingorumDash.png)

Maybe, one day, we'll manage to make this thing work...
