:: PATH-Variablen in das Projekt-Verzeichnis
set JAVA_PATH=C:/Program Files/Java/jre1.8.0_20/bin
set PROJ_PATH=D:/Dokumente/Eclipse/workspace/VS_Aufgabe_1/src;D:/Dokumente/Eclipse/workspace/VS_Aufgabe_1/bin
set BIN_PATH=D:/Dokumente/Eclipse/workspace/VS_Aufgabe_1/bin/

:: Vorhandene RMI Registry Instanzen beenden
taskkill /IM rmiregistry.exe /F

:: 1. RMI Registry starten
start "RMI Registry" cmd /c "%JAVA_PATH%/rmiregistry.exe" -J-Djava.rmi.server.codebase=file:///%BIN_PATH% & timeout /t 1 /nobreak >nul

:: 2. RMI Server starten (Parameter zum Setzen des Hosts: -Djava.rmi.server.hostname=localhost)
start "RMI Server " cmd /c "%JAVA_PATH%/java.exe" -cp %PROJ_PATH% -Djava.rmi.server.codebase=file:///%PROJ_PATH% -Djava.security.policy=%PROJ_PATH%/server.policy rmi.MessageServiceServer