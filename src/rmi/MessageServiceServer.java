package rmi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JOptionPane;


public class MessageServiceServer extends UnicastRemoteObject implements MessageService {

    private static final long serialVersionUID = 1L;
    private static final File logSend = new File("Server_Send_Messages.txt"); // FileHandler fuer die Log-Datei der gesendeten Nachrichten
    private static final File logRecv = new File("Server_Recv_Messages.txt"); // FileHandler fuer die Log-Datei der empfangenen Nachrichten
    private static MessageService messageService;
    private static String serverAddr;
    private final int deliveryQueueSize = 10; 
    private DeliveryQueue deliveryQueue = new DeliveryQueue(deliveryQueueSize);

    public MessageServiceServer() throws RemoteException {
        super();
    }

    @Override
    public String nextMessage(String clientID) {
        Message nextMessage = deliveryQueue.getMessage(clientID);
        System.out.println("nextMessage Methode wurde auf dem Server aufgerufen: " + clientID);
        writeLog(clientID, ((nextMessage == null) ? null : nextMessage.toString()), logSend);
        return ((nextMessage == null) ? null : nextMessage.toString());
    }

    @Override
    public void newMessage(String clientID, String message) {
        Message newMessage = new Message(clientID, message);
        System.out.println("newMessage Methode wurde auf dem Server aufgerufen: " + clientID + " \"" + message + "\"");
        deliveryQueue.addMessage(newMessage);
        writeLog(clientID, newMessage.toString(), logRecv);
    }

    private static void getGlobalIp() {
        // Globale IP-Adresse ermitteln
        try {
            serverAddr = InetAddress.getLocalHost().getHostName();
            System.out.println("Suche nach globaler IP-Adresse von " + serverAddr + "\n");

            boolean isSiteLocalAddress;
            boolean isLinkLocalAddress;
            boolean isLoopbackAddress;
            boolean isReachable;

            InetAddress [] inetAddresses = InetAddress.getAllByName(serverAddr);
            for (InetAddress inetAddress : inetAddresses) {
                System.out.println("Hostname:\t\t" + inetAddress.getHostName());
                System.out.println("IP-Adresse:\t\t" + inetAddress.getHostAddress());
                isSiteLocalAddress = inetAddress.isSiteLocalAddress();
                isLinkLocalAddress = inetAddress.isLinkLocalAddress();
                isLoopbackAddress = inetAddress.isLoopbackAddress();
                isReachable = inetAddress.isReachable(10000);
                System.out.println("isSiteLocalAddress:\t" + isSiteLocalAddress);
                System.out.println("isLinkLocalAddress:\t" + isLinkLocalAddress);
                System.out.println("isLoopbackAddress:\t" + isLoopbackAddress);
                System.out.println("isReachable:\t\t" + isReachable + "\n");

                // Address reachable & local (default: only reachable)
                if (isReachable && isSiteLocalAddress && !isLinkLocalAddress && !isLoopbackAddress) {
                    serverAddr = inetAddress.getHostAddress();
                }
            }
            System.setProperty("java.rmi.server.hostname", serverAddr);
            System.out.println("IP-Adresse fuer die Anmeldung an der RMI Registry: " + System.getProperty("java.rmi.server.hostname"));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Ermitteln der globalen IP-Adresse!", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.err.println("Fehler beim Ermitteln der globalen IP-Adresse!");
        }
    }

    private static void writeLog(String clientID, String message, File logfile) {
        try (PrintWriter logWriter = new PrintWriter(new FileOutputStream(logfile, true))) {
            Date date = new Date();
            DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
            String formattedMessage = new String(df.format(date) + " | " + clientID + ": " + message + "\n");
            logWriter.append(formattedMessage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Schreiben der Log-Datei!", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.err.println("Fehler beim Schreiben der Log-Datei!");
            e.printStackTrace();
        }
    }

    private static void exit() {
        // Server an der RMI Registry abmelden und entfernen
        try{
            Naming.unbind(serverAddr);
            UnicastRemoteObject.unexportObject(messageService, true);
            JOptionPane.showMessageDialog(null, "RMI Server wurde beendet!", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("RMI Server wurde beendet!");
        } catch(RemoteException | MalformedURLException | NotBoundException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Beenden des RMI Servers!", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.err.println("Fehler beim Beenden des RMI Servers!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        // Properties fuer die Verbindung zur RMI Registry setzen
        System.setProperty("sun.rmi.transport.connectionTimeout", "5000");      // Timeout bevor RMI einen inaktiven Socket schliesst und wieder freigibt. (Default: 15 Sekunden)
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "5000");   // Timeout fuer den Client beim Handshake mit dem Server. (Default: 60 Sekunden)
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");    // Timeout fuer das Warten eines Clients auf das Ergebnis eines Remote-Aufrufs. (Default: Kein Timeout)
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "5000");        // Timeout fuer das Lesen von Bytes, bevor RMI die Verbindung fuer ungueltig erklaert. Bei Timeout erfolgt eine RemoteException beim Client. (Default: 2 Stunden)

        // Anmeldung des Servers an der RMI Registry
//        getGlobalIp(); // Globale IP ermitteln
        serverAddr = "MessageService";
        Registry registry = null;        
        try {
            messageService = new MessageServiceServer();
            registry = LocateRegistry.getRegistry();
            registry.rebind(serverAddr, messageService);
            System.out.println("RMI Server wurde erfolgreich mit der Adresse \"" + serverAddr + "\" an der RMI Registry angemeldet!");
            JOptionPane.showMessageDialog(null, "RMI Server wurde erfolgreich mit der Adresse \"" + serverAddr + "\" an der RMI Registry angemeldet!", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException e) {
            System.err.println("RMI Server konnte nicht mit der Adresse \"" + serverAddr + "\" an der RMI Registry angemeldet werden!");
            JOptionPane.showMessageDialog(null, "RMI Server konnte nicht mit der Adresse \"" + serverAddr + "\" an der RMI Registry angemeldet werden!", "Fehler", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(0);
        }        
//        exit(); // Abmelden des Servers und Entfernen des Eintrags in der RMI Registry
    }
}