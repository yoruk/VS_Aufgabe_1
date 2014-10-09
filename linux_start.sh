#!/bin/sh

killall rmiregistry

PATH=/mnt/fileserver/MyHome/TI_Labor/Linux/eclipse44/VS_Aufgabe_1

# 1. RMI Registry starten (Lokale Codebase deaktivieren):
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false &

# 2. Server starten:
java -cp $PATH/bin \
     -Djava.rmi.server.useCodebaseOnly=false \
     -Djava.rmi.server.codebase=file:$PATH/bin/ \
     -Djava.security.policy=$PATH/rmi.policy \
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
