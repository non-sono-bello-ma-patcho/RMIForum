# RMICore
Core for the RMIForum repo

RMIUtiliy is the primitive class for RMIServer and Users wrappers, it implements all the main method to ensuring a working RMI server, it also implements methods to fetch other servers methods.

## Core.RMIUtility API
```java
void serverSetup(Remote toExport, String LocalHost);
void RMIShutDown(Remote toUnexport);
Remote getRemoteMethods(String Host);
```
