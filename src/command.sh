#!/bin/bash
[ -f /tmp/RMIServer.policy ] && sudo rm -f /tmp/RMIServer.policy
sudo echo -e "grant {\n\tpermission java.security.AllPermission;\n\tpermission java.net.SocketPermission \"localhost:$1\", \"connect, resolve\";\n\tpermission java.net.SocketPermission \"127.0.0.1:$1\", \"connect, resolve\";\n\tpermission java.net.SocketPermission \"localhost:$1\", \"connect, resolve\";\n};" > /tmp/RMIServer.policy
