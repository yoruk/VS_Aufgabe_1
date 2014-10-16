:: PATH-Variablen in das Projekt-Verzeichnis
set PROJ_PATH=C:/Users/Eugen Winter/Documents/Eclipse/workspace/VS_Aufgabe_1
set BIN_PATH=C:/Users/Eugen Winter/Documents/Eclipse/workspace/VS_Aufgabe_1/bin/

:: Vorhandene RMI Registry Instanzen beenden
taskkill /IM rmiregistry.exe /F

:: 1. Server samt RMI Registry starten
start "RMI Server" cmd /c java -cp "%BIN_PATH%" -Djava.security.policy=file:///"%PROJ_PATH%/server.policy" rmi.MessageServiceServer

:: 2. Client starten
start "RMI Client" cmd /c java -cp "%BIN_PATH%" -Djava.security.policy=file:///"%PROJ_PATH%/client.policy" rmi.MessageServiceClient