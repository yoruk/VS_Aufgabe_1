#!/bin/sh

# Vorhandene RMI Registry Instanzen beenden
killall rmiregistry

# PATH-Variablen in das Projekt-Verzeichnis setzen
PATH=/mnt/fileserver/MyHome/TI_Labor/Linux/eclipse44/VS_Aufgabe_1:$PATH


# 1. Server samt RMI Registry starten
java -cp $PATH/bin/ \
     -Djava.security.policy=$PATH/server.policy \
     rmi.MessageServiceServer

# 2. Client starten
java -cp $PATH/bin/ \
     -Djava.security.policy=$PATH/client.policy \
     rmi.MessageServiceClient