#!/usr/bin/sh

# Vorhandene RMI Registry Instanzen beenden
killall rmiregistry

# PATH-Variablen in das Projekt-Verzeichnis setzen
PATH=/mnt/fileserver/MyHome/TI_Labor/Linux/eclipse44/VS_Aufgabe_1/src:/mnt/fileserver/MyHome/TI_Labor/Linux/eclipse44/VS_Aufgabe_1/bin:$PATH

# 1. RMI Registry starten
rmiregistry -J-Djava.rmi.server.codebase=file:///$PATH/ &

# 2. RMI Server starten (Parameter zum Setzen des Hosts: -Djava.rmi.server.hostname=localhost)
java -cp $PATH/bin \
     -Djava.rmi.server.codebase=file:///$PATH \
     -Djava.security.policy=$PATH/server.policy \
     rmi.MessageServiceImpl

# 3. Client starten (Parameter zum Setzen des Hosts und des Ports: localhost 1099)
#java -cp $PATH/bin \
#     -Djava.rmi.server.codebase=file:///$PATH \
#     -Djava.security.policy=$PATH/client.policy \
#     rmi.MessageServiceClient
