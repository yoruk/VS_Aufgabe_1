package common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

    /* Class variables */
    private static final File logFile = new File("client_sent_msgs.txt"); // File handler for the logfile of all sent messages
    private static StringBuilder stringBuffer = new StringBuilder();      // Buffer for received messages from the server
    private static MessageService messageService; // MessageService object for remote connection to the RMI registry
    private static Registry registry;             // RMI registry for the lookup of a server
    private static String messageToSend;          // Message being send to the server
    private static String serverAddr;             // Address of the server
    private static String serverName;             // Name of the server in the connected RMI registry
    private static String clientID;               // Name of the client
    private static int serverTimeout;             // Time the client try to reconnect to the server and keep the connection if there are connectivity issues (in seconds)
    private static boolean isResending;           // Boolean flag to check if the attempt to send a message to the server failed and has to be repeated
    private static JTextField textServerAddr;     // Text field for server address
    private static JTextField textServerName;     // Text field for server name in the remote RMI registry
    private static JTextField textClientID;       // Text field for client name
    private static JTextField textServerTimeout;  // Text field for server connection timeout
    private static JTextArea textOutputArea;      // Text area for displaying received messages
    private static JTextArea textInputArea;       // Text area for entering messages to be send
    private static JButton buttonConnect;         // Button for connecting to the server
    private static JButton buttonDisconnect;      // Button for disconnecting from the server
    private static JButton buttonSend;            // Button for sending messages to the server
    private static JButton buttonRecv;            // Button for receiving messages from the server

    /* This method creates and shows the GUI window */
    private static void createAndShowGUI() {

        JFrame frame = new JFrame("MessageService Client");          // Window name
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        // Operation when "close" button is clicked
        frame.setIconImage(frame.getToolkit().getImage("icon.png")); // Application icon
        frame.setMinimumSize(new Dimension(800, 600));               // Window size
        frame.setResizable(false);                                   // Window resizing

        // Create top panel components
        JPanel panelTop = new JPanel(new GridLayout(5, 2));
        panelTop.setBorder(BorderFactory.createEtchedBorder(1));
        JLabel labelServerAddr = new JLabel("Server address:");
        JLabel labelServerName = new JLabel("Server name:");
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

        // Create center panel components
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

        // Create bottom panel components
        JPanel panelBottom = new JPanel();
        panelBottom.setBorder(BorderFactory.createEtchedBorder(1));
        textInputArea = new JTextArea(5, 62);
        textInputArea.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(1, 3, 1, 1)));
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
                sendMessage();
            }
        });

        buttonRecv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiveMessage();
            }
        });

        // Create ActionListener for the text input area as anonymous class
        textInputArea.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    e.consume(); // Consume ENTER key to prevent a newline in text input area
                    buttonSend.doClick(); // Send messages with enter key
                }
            }

            @Override
            public void keyReleased(KeyEvent e) { } // Do nothing

            @Override
            public void keyTyped(KeyEvent e) { } // Do nothing
        });

        // Add panels to the frame and make it visible
        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
        frame.getContentPane().add(panelBottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.validate();
        frame.setVisible(true);
    } /* createAndShowGUI */

    /* This method is saving a given string message to a logfile */
    private static void writeLog(String message) {
        
        try (PrintWriter logWriter = new PrintWriter(new FileOutputStream(logFile, true))) {
            Date date = new Date();
            DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
            String formattedMessage = new String(df.format(date) + " | " + clientID + ": " + message + "\n");
            logWriter.append(formattedMessage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to write log-file \"" + logFile.getAbsolutePath() + "\"!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } /* writeLog */

    /* This method establishes a connection to the specified RMI server */
    private static void connect() {

        // Read content of text fields into static variables
        serverAddr = textServerAddr.getText();
        serverName = textServerName.getText();
        clientID = textClientID.getText();
        serverTimeout = Integer.parseInt(textServerTimeout.getText());

        // Install security manager
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // Lookup the server in the remote RMI registry by its name
        try {
            registry = LocateRegistry.getRegistry(serverAddr);
            messageService = (MessageService) registry.lookup(serverName);
            JOptionPane.showMessageDialog(null, "Successfully connected to server \"" + serverAddr + "\"!", "Information", JOptionPane.INFORMATION_MESSAGE);

            // Set GUI components
            textServerName.setEnabled(false);
            textServerAddr.setEnabled(false);
            textClientID.setEnabled(false);
            textServerTimeout.setEnabled(false);
            textInputArea.setEnabled(true);
            buttonConnect.setEnabled(false);
            buttonDisconnect.setEnabled(true);
            buttonSend.setEnabled(true);
            buttonRecv.setEnabled(true);
            isResending = false;

        } catch (AccessException e) {
            JOptionPane.showMessageDialog(null, "Unable to access server \"" + serverAddr + "\" in the RMI registry!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NotBoundException e) {
            JOptionPane.showMessageDialog(null, "Unable to find server \"" + serverAddr + "\" in the RMI registry!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the RMI registry!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } /* connect */

    /* This method "disconnects" the client from the server. In fact it resets the GUI components and logs, so the client can (re-)connect to a(-nother) server again */
    private static void disconnect() {
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
        stringBuffer = new StringBuilder();
        textInputArea.setText("");
        textOutputArea.setText("");
        isResending = false;
    } /* disconneect */

    /* This method tests the server connection by fetching a list of all servers registered to the RMI registry and compare it to the used "serverName" */
    private static boolean testConnection() {

        List<String> serverList = new ArrayList<String>();

        try {
            Collections.addAll(serverList, registry.list()); // Fetch list of all servers registered to the RMI registry
        } catch (AccessException e) {
            JOptionPane.showMessageDialog(null, "Unable to access the RMI registry to test the connection to the server!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the RMI registry to test the connection to the server!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            disconnect();
        }

        return serverList.contains(serverName);
    } /* testConnection*/

    /* This method waits and tries for "serverTimeout" seconds to reestablish the connection to the server and to resend a message for a second time, before disconnecting from the server */
    private static void resendMessage() {

        JOptionPane.showMessageDialog(null, "First attempt to send the message to server \"" + serverAddr + "\" failed.\nClick OK to attempt to resend the message for " + serverTimeout + " seconds!", "Information", JOptionPane.INFORMATION_MESSAGE);

        for(int i = 0; i < serverTimeout; i++) {
            if(testConnection()) {
                JOptionPane.showMessageDialog(null, "Reconnecting to server \"" + serverAddr + "\" was successful. Resending the message now!", "Information", JOptionPane.INFORMATION_MESSAGE);
                sendMessage(); // Resend last message
                return;
            } else {
                try {
                    Thread.sleep(1000); // Attempt to reestablish the connection to the server every second of the "serverTimeout" time
                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(null, "Waiting for server \"" + serverAddr + "\" was interrupted. Disconnecting now!", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    disconnect();
                }
            }
        } // Reached end of the "serverTimeout" time. Server ist still unreachable!

        // Check for a last time if the server is still unreachable and else disconnect from the server
        if(!testConnection()) {
            JOptionPane.showMessageDialog(null, "Reconnecting to server \"" + serverAddr + "\" has failed.\nUnable to resend the message! Disconnecting now!", "Error", JOptionPane.ERROR_MESSAGE);
            disconnect();
        }
    } /* keepConnection */

    /* This method sends a message written in the text input area to the server */
    private static void sendMessage() {

        // Read content of text fields into static variables
        messageToSend = (isResending == false) ? textInputArea.getText() : messageToSend; // Use old value of messageToSend during resending process
        textInputArea.setText(""); // Clear input text area

        // Try to send message, else try to keep the connection
        if(testConnection()) {
            try {
                messageService.newMessage(clientID, messageToSend);
                isResending = false;
            } catch (RemoteException e) {
                if(isResending == true) {
                    JOptionPane.showMessageDialog(null, "Second attempt to send the message to server \"" + serverAddr + "\" failed. Disconnecting now!", "Error", JOptionPane.ERROR_MESSAGE);
                    disconnect();
                } else {
                    isResending = true; // First attempt to send the message failed because of a remote exception, try to resend the message
                    resendMessage();
                }
                e.printStackTrace();
            }

            writeLog(messageToSend); // Write messageToSend into the local logfile

        } else {
            if(isResending == true) {
                JOptionPane.showMessageDialog(null, "Second attempt to send the message to server \"" + serverAddr + "\" failed. Disconnecting now!", "Error", JOptionPane.ERROR_MESSAGE);
                disconnect();
            } else {
                isResending = true; // First attempt to send the message failed because of an unreachable server, try to resend the message
                resendMessage();
            }
        }
    } /* sendMessage */

    /* This method receives all possible messages with a valid timestamp from the server */
    private static void receiveMessage() {

        String tempMessage;

        // Try to receive all messages, else try to keep the connection before disconnecting from the server
        if(testConnection()) {
            try {                

                do {
                    tempMessage = messageService.nextMessage(clientID);
                    
                    if(tempMessage != null) {
                        stringBuffer.append(tempMessage + "\n"); // Append received messages to "stringBuffer"
                    }                    
                } while (tempMessage != null);

            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(null, "Unable to reach server \"" + serverAddr + "\" for new messages to receive!", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

            textOutputArea.setText(stringBuffer.toString());
        } else {
            JOptionPane.showMessageDialog(null, "Unable to receive messages from server \"" + serverAddr + "\"!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } /* receiveMessage */

    public static void main(String[] args) {

        // Run GUI as an EventDispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });

    }

}