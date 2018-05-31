# RMIForum
Simple Java based Forum

This is a Work in progress, stay tuned
## RMIServer API
### Exportable methods:
```java
boolean ManageConnection(String username, String pw, String adress, String op);
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
