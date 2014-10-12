package rmi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;


public class MessageServiceClient {

    /* OBJEKTVARIABLEN */
    private static final File logFile = new File("Client_Send_Messages.txt");     // FileHandler fuer die Log-Datei der gesendeten Nachrichten
    private static MessageService messageService;                           // MessageService Objekt
    private static Registry registry;                                       // Registry Objekt fuer RMI

    private static String hostname;                                         // Adresse des Servers
    private static String clientID;                                         // Zu sendende Client-ID an den Server
    private static String messageToSend;                                    // Zu sendender Text an den Server
    private static String messageToRecv;                                    // Zu empfangender Text vom Server
    private static StringBuilder messagesReceived = new StringBuilder("");  // Log fuer empfangene Nachrichten
    private static List<String> serverList = new ArrayList<String>();       // Liste aller Server, die in der RMI Registry gelistet sind
    private static final int S = 5;                                         // Ausfallzeit s in Sekunden

    private static JTextField textHostname;                                 // Eingabefeld fuer Servernamen
    private static JTextField textClientID;                                 // Eingabefeld fuer Clientnamen
    private static JTextArea textOutputArea;                                // Textfeld fuer empfangene Nachrichten
    private static JTextArea textInputArea;                                 // Eingabebereich fuer die Nachrichten des Clients
    private static JButton buttonConnect;                                   // Button fuer den Verbindungsaufbau zum Server
    private static JButton buttonDisconnect;                                // Button fuer den Verbindungsabbau vom Server
    private static JButton buttonSend;                                      // Button zum Senden von Nachrichten zum Server
    private static JButton buttonRecv;                                      // Button zum Empfangen von Nachrichten vom Server

    private static void writeLog(String message) {
        try (PrintWriter logWriter = new PrintWriter(new FileOutputStream(logFile, true))) {
            Date date = new Date();
            DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
            String formattedMessage = new String(df.format(date) + " | " + clientID + ": " + message + "\n");
            logWriter.append(formattedMessage);
        } catch (IOException e) {
            showErrorMessage("Fehler beim Schreiben der Log-Datei!");
            System.err.println("Fehler beim Schreiben der Log-Datei!");
            e.printStackTrace();
        }
    }

    private static void showErrorMessage(String message) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, "FEHLER", JOptionPane.ERROR_MESSAGE);
                //                System.exit(0);
            }
        });
    }

    private static void disconnect() {

        // GUI aktualisieren
        textHostname.setEnabled(true);
        textClientID.setEnabled(true);
        textInputArea.setEnabled(false);
        buttonConnect.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        buttonSend.setEnabled(false);
        buttonRecv.setEnabled(false);
        messageToSend = new String();
        messageToRecv = new String();
        messagesReceived = new StringBuilder();
        textInputArea.setText("");
        textOutputArea.setText("");
    }

    private static void connect() {

        // GUI aktualisieren
        textHostname.setEnabled(false);
        textClientID.setEnabled(false);
        textInputArea.setEnabled(true);
        buttonConnect.setEnabled(false);
        buttonDisconnect.setEnabled(true);
        buttonSend.setEnabled(true);
        buttonRecv.setEnabled(true);

        // Verbindungsinformationen fuer das Lookup aus der GUI auslesen
        hostname = textHostname.getText();
        clientID = textClientID.getText();

        // Lookup des Servers in der RMI Registry durchfuehren
        try {
            registry = LocateRegistry.getRegistry();
            messageService = (MessageService) registry.lookup(hostname);
        } catch (AccessException e) {
            showErrorMessage("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            System.err.println("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            e.printStackTrace();
        } catch (NotBoundException e) {
            showErrorMessage("Das Remote Objekt kann nicht in der RMI Registry gefunden werden!");
            System.err.println("Das Remote Objekt konnte nicht in der RMI Registry gefunden werden!");
            disconnect();
            e.printStackTrace();
        } catch (RemoteException e) {
            showErrorMessage("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            System.err.println("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            disconnect();
            e.printStackTrace();
        }
    }

    private static boolean testConnection() {
        //TODO keepConnection und testConnection finalisieren
        try {
            serverList.clear(); // Liste der Server zuvor leeren
            Collections.addAll(serverList, registry.list()); // Schauen, ob der Server noch in der RMI Registry gelistet ist
        } catch (AccessException e) {
            showErrorMessage("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            System.err.println("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            disconnect();
            e.printStackTrace();
        } catch (RemoteException e) {
            showErrorMessage("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            System.err.println("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            disconnect();
            e.printStackTrace();
        }
        return serverList.contains(hostname);
    }

    private static void keepConnection() {
        //TODO Eventuell muss diese Funktion per separatem Thread implementiert werden
        try {
            showErrorMessage("Server \"" + hostname + "\" antwortet nicht. Warte fuer " + S + " Sekunden!");
            Thread.sleep(S * 1000);
        } catch (InterruptedException e) {
            showErrorMessage("Fehler beim Abwarten der Ausfallzeit!");
            System.err.println("Fehler beim Abwarten der Ausfallzeit!");
            disconnect();
            e.printStackTrace();
        }

        // Testen, ob der Server wieder in der RMI Registry verfuegbar ist
        try {
            serverList.clear(); // Liste der Server zurvor leeren
            Collections.addAll(serverList, registry.list());
        } catch (AccessException e) {
            showErrorMessage("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            System.err.println("Fehler beim Zugriff auf den Server \"" + hostname + "\"!");
            disconnect();
            e.printStackTrace();
        } catch (RemoteException e) {
            showErrorMessage("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            System.err.println("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            disconnect();
            e.printStackTrace();
        }

        // Fehlerdialog zeigen und Programm beenden
        if(!(serverList.contains(hostname))) {
            showErrorMessage("Server antwortet seit " + S + " Sekunden nicht mehr!");
        }
    }

    private static void send() {
        messageToSend = textInputArea.getText();
        textInputArea.setText("");

        if(testConnection()) {
            try {
                messageService.newMessage(clientID, messageToSend);
            } catch (RemoteException e) {
                showErrorMessage("Fehler beim Abholen der Nachricht von Server \"" + hostname + "\"!");
                System.err.println("Fehler beim Abholen der Nachricht von Server \"" + hostname + "\"!");
                e.printStackTrace();
            }
            writeLog(messageToSend);
        } else {
            keepConnection(); // Versuchen die Verbindung zu halten
        }
    }

    private static void receive() {
        testConnection(); //Testen, ob der Server noch in der RMI Registry verfuegbar ist

        try {
            messageToRecv = messageService.nextMessage(clientID);
        } catch (RemoteException e) {
            showErrorMessage("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            System.err.println("Fehler bei der Kommunikation mit dem Server \"" + hostname + "\"!");
            e.printStackTrace();
        }
        messagesReceived.append(messageToRecv).append("\n");
        textOutputArea.setText(messagesReceived.toString());
    }

    private static void createAndRunGUI() {

        // Fenster erstellen
        JFrame frame = new JFrame("MessageService GUI");                // Name des Fenstertitels
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);           // Aktion beim Druecken des Schliessen-Knopfs
        frame.setIconImage(frame.getToolkit().getImage("icon.png"));    // App-Icon
        frame.setMinimumSize(new Dimension(800, 600));                  // Groesse
        frame.setResizable(false);                                      // Aendern der Fenstergroesse deaktivieren

        // Top Panel erstellen
        JPanel panelTop = new JPanel(new GridLayout(2, 2));
        panelTop.setBorder(BorderFactory.createEtchedBorder(1));
        JLabel labelhostname = new JLabel("Hostname:");
        JLabel labelClientID = new JLabel("Client-ID:");
        textHostname = new JTextField("MessageService");
        textClientID = new JTextField("Nickname");
        buttonConnect = new JButton("Connect");
        buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.setEnabled(false);
        panelTop.add(labelhostname);
        panelTop.add(textHostname);
        panelTop.add(buttonConnect);
        panelTop.add(labelClientID);
        panelTop.add(textClientID);
        panelTop.add(buttonDisconnect);

        // Center Panel erstellen
        JPanel panelCenter = new JPanel();
        panelCenter.setBorder(BorderFactory.createEtchedBorder(1));
        textOutputArea = new JTextArea(26, 76);
        textOutputArea.setEnabled(false);
        JScrollPane scrollPaneCenter = new JScrollPane(textOutputArea);
        scrollPaneCenter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneCenter.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelCenter.add(scrollPaneCenter);

        // Bottom Panel erstellen
        JPanel panelBottom = new JPanel();
        panelBottom.setBorder(BorderFactory.createEtchedBorder(1));
        textInputArea = new JTextArea(5, 62);
        textInputArea.setEditable(true);
        textInputArea.setEnabled(false);
        JScrollPane scrollPaneBottom = new JScrollPane(textInputArea);
        scrollPaneBottom.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneBottom.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        buttonSend = new JButton("Send");
        buttonSend.setEnabled(false);
        buttonRecv = new JButton("Receive");
        buttonRecv.setEnabled(false);
        panelBottom.add(scrollPaneBottom);
        panelBottom.add(buttonSend);
        panelBottom.add(buttonRecv);


        // ActionListener als anonyme Klassen erstellen
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        buttonDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        buttonSend.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        buttonRecv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receive();
            }
        });

        // Panels zum Fenster hinzufuegen und das Fenster zeichnen
        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
        frame.getContentPane().add(panelBottom, BorderLayout.SOUTH);
        frame.pack();                       // Fenstergroesse optimal anpassen
        frame.setLocationRelativeTo(null);  // Programmfenster zentriert starten
        frame.validate();                   // Container ausrichten
        frame.setVisible(true);             // Feinster sichtbar machen
    }

    public static void main(String[] args) {

        // Wegen der Threadsicherheit wird die Methode fuer die GUI-Erzeugung EventDispatching-Thread gestartet
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndRunGUI();
            }
        });
    }
}
