package rmi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MessageServiceClient {

    // Class variables
    private static final File logFile = new File("Client_Sent_Messages.txt");   // FileHandler fuer die Log-Datei der gesendeten Nachrichten
    private static StringBuilder messagesReceived = new StringBuilder("");      // Log fuer empfangene Nachrichten
    private static List<String> serverList = new ArrayList<String>();           // Liste aller Server, die in der RMI Registry gelistet sind
    private static MessageService messageService;   // MessageService Objekt
    private static Registry registry;               // Registry Objekt fuer RMI
    private static String messageToSend;            // Zu sendender Text an den Server
    private static String messageToRecv;            // Zu empfangender Text vom Server
    private static String serverAddr;               // Adresse des Servers
    private static String serverName;               // Name of the server in the remote RMI registry
    private static String clientID;                 // Zu sendende Client-ID an den Server
    private static int serverTimeout;               // Ausfallzeit s in Sekunden
    private static JFrame frame;                    // Main programm window
    private static JTextField textServerName;       // Name for the remote instance to seatch for in the remote RMI registry
    private static JTextField textServerAddr;       // Eingabefeld fuer Servernamen
    private static JTextField textClientID;         // Eingabefeld fuer Clientnamen
    private static JTextField textServerTimeout;    // Eingabefeld fuer Clientnamen
    private static JTextArea textOutputArea;        // Textfeld fuer empfangene Nachrichten
    private static JTextArea textInputArea;         // Eingabebereich fuer die Nachrichten des Clients
    private static JButton buttonConnect;           // Button fuer den Verbindungsaufbau zum Server
    private static JButton buttonDisconnect;        // Button fuer den Verbindungsabbau vom Server
    private static JButton buttonSend;              // Button zum Senden von Nachrichten zum Server
    private static JButton buttonRecv;              // Button zum Empfangen von Nachrichten vom Server

    // Create GUI window
    private static void createAndRunGUI() {

        frame = new JFrame("MessageService Client");                    // Name des Fenstertitels
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);           // Aktion beim Druecken des Schliessen-Knopfs
        frame.setIconImage(frame.getToolkit().getImage("icon.png"));    // App-Icon
        frame.setMinimumSize(new Dimension(800, 600));                  // Groesse
        frame.setResizable(false);                                      // Aendern der Fenstergroesse deaktivieren

        // Create top panel
        JPanel panelTop = new JPanel(new GridLayout(5, 2));
        panelTop.setBorder(BorderFactory.createEtchedBorder(1));
        JLabel labelServerAddr = new JLabel("Server address:");
        JLabel labelServerName = new JLabel("Server name:");
        labelServerName.setToolTipText("Name of the instance that should be searched in the remote RMI registry");
        JLabel labelClientID = new JLabel("Client-ID:");
        JLabel labelServerTimeout = new JLabel("Server connection timeout:");
        textServerAddr = new JTextField("localhost");
        textServerName = new JTextField("MessageService");
        textClientID = new JTextField("Nickname");
        textServerTimeout = new JTextField("5");
        buttonConnect = new JButton("Connect");
        buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.setEnabled(false);
        panelTop.add(labelServerAddr);
        panelTop.add(textServerAddr);
        panelTop.add(labelServerName);
        panelTop.add(textServerName);
        panelTop.add(labelClientID);
        panelTop.add(textClientID);
        panelTop.add(labelServerTimeout);
        panelTop.add(textServerTimeout);
        panelTop.add(buttonConnect);
        panelTop.add(buttonDisconnect);

        // Create center panel
        JPanel panelCenter = new JPanel();
        panelCenter.setBorder(BorderFactory.createEtchedBorder(1));
        textOutputArea = new JTextArea(26, 76);
        textOutputArea.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(1, 3, 1, 1)));
        textOutputArea.setEditable(false);
        textOutputArea.setForeground(Color.BLUE);
        textOutputArea.setBackground(Color.WHITE);
        JScrollPane scrollPaneCenter = new JScrollPane(textOutputArea);
        scrollPaneCenter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneCenter.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelCenter.add(scrollPaneCenter);

        // Create bottom panel
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


        // Create ActionListener for the buttons as anonymous classes
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

        // Add panels to the window and make it visible
        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
        frame.getContentPane().add(panelBottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.validate();
        frame.setVisible(true);
    } // createAndRunGUI

    // Log messages to specified log-file
    private static void writeLog(String message) {
        try (PrintWriter logWriter = new PrintWriter(new FileOutputStream(logFile, true))) {
            Date date = new Date();
            DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
            String formattedMessage = new String(df.format(date) + " | " + clientID + ": " + message + "\n");
            logWriter.append(formattedMessage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to write log-file \"" + logFile.getAbsolutePath() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } // writeLog

    // Connecto to server
    private static void connect() {

        // Read content of text-fields into static variables
        serverAddr = textServerAddr.getText();
        serverName = textServerName.getText();
        clientID = textClientID.getText();
        serverTimeout = Integer.parseInt(textServerTimeout.getText());

        // Install security manager
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // Lookup remote RMI registry server for the specified name
        try {
            registry = LocateRegistry.getRegistry(serverAddr);
            messageService = (MessageService) registry.lookup(serverName);
            JOptionPane.showMessageDialog(null, "Successfully connected to server \"" + serverAddr + "\"", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (AccessException e) {
            JOptionPane.showMessageDialog(null, "Unable to access server \"" + serverAddr + "\" in the RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        } catch (NotBoundException e) {
            JOptionPane.showMessageDialog(null, "Unable to find server \"" + serverAddr + "\" in the RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        }

        // Set GUI elements
        textServerName.setEnabled(false);
        textServerAddr.setEnabled(false);
        textClientID.setEnabled(false);
        textServerTimeout.setEnabled(false);
        textInputArea.setEnabled(true);
        buttonConnect.setEnabled(false);
        buttonDisconnect.setEnabled(true);
        buttonSend.setEnabled(true);
        buttonRecv.setEnabled(true);
    } // connect

    // Disconnect from server
    private static void disconnect() {

        // Set GUI elements
        textServerName.setEnabled(true);
        textServerAddr.setEnabled(true);
        textClientID.setEnabled(true);
        textServerTimeout.setEnabled(true);
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
    } // disconneect

    //TODO keepConnection und testConnection finalisieren
    // private static boolean isSent; // EVENTUELL FUER FEHLERSEMANTIK ALS ZAEHLVARIABLE FUER AT-LEAST-ONCE
    // Return TRUE if connection is still available an the server is still listed in the RMI registry
    private static boolean testConnection() {

        // Clear server list before fetching a new one with servers bounded to the RMI registry
        serverList.clear();

        try {
            Collections.addAll(serverList, registry.list());
        } catch (AccessException e) {
            JOptionPane.showMessageDialog(null, "Unable to access RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        }
        return serverList.contains(serverName);
    }

    //TODO Eventuell muss diese Funktion per separatem Thread implementiert werden?!
    // Try to keep connection for "serverTimeout" seconds before disconnect
    private static void keepConnection() {
        JOptionPane.showMessageDialog(null, "Server \"" + serverAddr + "\" is not reponding. Waiting for " + serverTimeout + " seconds before next attempt", "Information", JOptionPane.INFORMATION_MESSAGE);
        try {
            Thread.sleep(serverTimeout * 1000);
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Unable to wait for " + serverTimeout + " seconds for the server \"" + serverAddr + "\" to respond", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        }

        // Check if connection is available again, else disconnect from the server
        if(!testConnection()) {
            JOptionPane.showMessageDialog(null, "Server \"" + serverAddr + "\" is still not reponding since " + serverTimeout + " seconds. Disconnecting now!", "Error", JOptionPane.ERROR_MESSAGE);
            disconnect();
        }
    } // keepConnection

    // Send messages to server
    private static void send() {

        // Read content of text-fields into static variables
        messageToSend = textInputArea.getText();
        textInputArea.setText("");

        // Try to send message, else try to keep the connection
        if(testConnection()) {
            try {
                messageService.newMessage(clientID, messageToSend);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(null, "Unable to send message to server \"" + serverAddr + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                keepConnection();
            }
            writeLog(messageToSend);
        } else {
            keepConnection(); // Try to keep connection
        }
    } // send

    // Receive messages from server
    private static void receive() {

        // Try to receive message, else try to keep the connection
        if(testConnection()) {
            try {
                messageToRecv = messageService.nextMessage(clientID);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(null, "Unable to receive message from server \"" + serverAddr + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            messagesReceived.append(messageToRecv).append("\n");
            textOutputArea.setText(messagesReceived.toString());
        } else {
            keepConnection();
        }
    } // receive

    // main
    public static void main(String[] args) {

        // Run GUI as an EventDispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndRunGUI();
            }
        });
    } // main

}