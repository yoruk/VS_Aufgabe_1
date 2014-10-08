#!/bin/sh

killall rmiregistry

PATH_PROJECT=/mnt/fileserver/MyHome/TI_Labor/Linux/eclipse44/VS_Aufgabe_1
PATH_BIN=$PATH_PROJECT/bin

# 1. RMI Registry starten (Lokale Codebase deaktivieren):
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false &

# 2. Server starten:
java -cp $PATH_BIN \
     -Djava.rmi.server.useCodebaseOnly=false \
     -Djava.rmi.server.codebase=file:$PATH_BIN/ \
     -Djava.security.policy=$PATH_PROJECT/rmi.policy \
     rmi.MessageServiceImpl


# Alternativ (Mit Host-Server Angabe):
#java -cp D:\Dokumente\Eclipse\workspace\VS_Aufgabe_1\bin \
#     -Djava.rmi.server.useCodebaseOnly=false \
#     -Djava.rmi.server.codebase=file:/D:\Dokumente\Eclipse\workspace\VS_Aufgabe_1\bin\ \
#     -Djava.rmi.server.hostname=localhost \
#     -Djava.security.policy=D:\Dokumente\Eclipse\workspace\VS_Aufgabe_1\rmi.policy \
#     rmi.MessageServiceImpl

# 3. Client starten:
# java -cp D:\Dokumente\Eclipse\workspace\VSP_01\bin \
# rmi.MessageServiceClient
