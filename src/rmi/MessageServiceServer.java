package rmi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.util.Date;
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
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MessageServiceServer extends UnicastRemoteObject implements MessageService {

    // Class variables
    private static final long serialVersionUID = 1L;
    private static final File logSend = new File("Server_Sent_Messages.txt");
    private static final File logRecv = new File("Server_Recv_Messages.txt");
    private static MessageService messageService;
    private static DeliveryQueue deliveryQueue;
    private static Registry registry;
    private static String serverName;
    private static int ttl;
    private static int deliveryQueueSize;
    private static JTextField textServerName;
    private static JTextField textQueueSize;
    private static JTextField textTTL;
    private static JTextArea textOutputArea;
    private static JButton buttonStart;
    private static JButton buttonStop;
    private static JButton buttonFindIp;
    private static JButton buttonStartRegistry;

    // Constructor
    public MessageServiceServer() throws RemoteException {
        super();
    }

    // Private class to redirect System.out stream to the JTextArea of the GUI
    private static class TextAreaPrintStream extends PrintStream {

        private JTextArea textArea;

        public TextAreaPrintStream(JTextArea area, OutputStream out) {
            super(out);
            textArea = area;
        }

        @Override
        public void println(String string) {
            textArea.append(string+"\n");
        }

        @Override
        public void print(String string) {
            textArea.append(string);
        }
    }


    /*vvvvvvvvvv     INTERFACE METHODS IMPLEMENTATION     vvvvvvvvvv*/

    @Override
    public String nextMessage(String clientID) {
        Message nextMessage = deliveryQueue.getMessage(clientID);                               // Get message from delivery queue
        System.out.println("<< nextMessage call by " + clientID);                               // Print method-call to GUI
        writeLog(clientID, ((nextMessage == null) ? null : nextMessage.toString()), logSend);   // Write log entry
        return ((nextMessage == null) ? null : nextMessage.toString());                         // Return fetched message
    }

    @Override
    public void newMessage(String clientID, String message) {
        Message newMessage = new Message(clientID, message, ttl);                               // Create new message
        System.out.println(">> newMessage call by " + clientID + ": \"" + message + "\"");      // Print method-call to GUI
        writeLog(clientID, newMessage.toString(), logRecv);                                     // Write log entry
        deliveryQueue.addMessage(newMessage);                                                   // Add new message to delivery queue
    }

    /*^^^^^^^^^^     INTERFACE METHODS IMPLEMENTATION     ^^^^^^^^^^*/


    // Create GUI window
    private static void createAndRunGUI() {

        JFrame frame = new JFrame("MessageService Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(frame.getToolkit().getImage("icon.png"));
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setResizable(false);

        // Create top panel
        JPanel panelTop = new JPanel(new GridLayout(5, 2));
        panelTop.setBorder(BorderFactory.createEtchedBorder(1));
        JLabel labelServerName = new JLabel("Server name:");
        labelServerName.setToolTipText("Name that should be provided to the RMI registry for this instance");
        textServerName = new JTextField("MessageService");
        textServerName.setEnabled(false);
        JLabel labelQueueSize = new JLabel("Queue-Size:");
        textQueueSize = new JTextField("10");
        textQueueSize.setEnabled(false);
        JLabel labelTTL = new JLabel("Message TTL (in seconds):");
        textTTL = new JTextField("60");
        textTTL.setEnabled(false);
        buttonStartRegistry = new JButton("Create RMI registry");
        buttonStart = new JButton("Start Server");
        buttonStart.setEnabled(false);
        buttonStop = new JButton("Stop Server");
        buttonStop.setEnabled(false);
        buttonFindIp = new JButton("Find IP-addresses");
        buttonFindIp.setEnabled(false);
        panelTop.add(labelServerName);
        panelTop.add(textServerName);
        panelTop.add(labelQueueSize);
        panelTop.add(textQueueSize);
        panelTop.add(labelTTL);
        panelTop.add(textTTL);
        panelTop.add(buttonStartRegistry);
        panelTop.add(buttonFindIp);
        panelTop.add(buttonStart);
        panelTop.add(buttonStop);

        // Create center panel
        JPanel panelCenter = new JPanel(new FlowLayout());
        panelCenter.setBorder(BorderFactory.createEtchedBorder(1));
        textOutputArea = new JTextArea(30, 76);
        textOutputArea.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(1, 3, 1, 1)));
        textOutputArea.setEditable(false);
        textOutputArea.setForeground(Color.RED);
        textOutputArea.setBackground(Color.WHITE);
        TextAreaPrintStream textPrintStream = new TextAreaPrintStream(textOutputArea, System.out);
        System.setOut(textPrintStream); // Redirect System.out-stream to the GUI
        JScrollPane scrollPaneCenter = new JScrollPane(textOutputArea);
        scrollPaneCenter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneCenter.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelCenter.add(scrollPaneCenter);

        // Create ActionListener for the buttons as anonymous classes
        buttonStartRegistry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startRegistry();
            }
        });

        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        buttonFindIp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findIpAddr();
            }
        });

        // Add panels to the window and make it visible
        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.validate();
        frame.setVisible(true);
    } // createAndRunGUI

    // Search for all possible IP-addresses for the current host and show them in a dialog after "Find IP-addresses" button was clicked
    private static void findIpAddr() {

        JFrame frame = new JFrame("IP-Addresses");
        StringBuilder sb = new StringBuilder("");
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane);

        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            InetAddress [] inetAddresses = InetAddress.getAllByName(hostname);
            boolean isSiteLocalAddress;
            boolean isLinkLocalAddress;
            boolean isLoopbackAddress;
            boolean isReachable;

            sb.append("Searching IP-Addesses for \"" + hostname + "\"\n\n");
            for (InetAddress inetAddress : inetAddresses) {
                sb.append("IP-Address:\t\t" + inetAddress.getHostAddress() + "\n");
                isSiteLocalAddress = inetAddress.isSiteLocalAddress();
                isLinkLocalAddress = inetAddress.isLinkLocalAddress();
                isLoopbackAddress = inetAddress.isLoopbackAddress();
                isReachable = inetAddress.isReachable(10000);
                sb.append("isSiteLocalAddress:\t" + isSiteLocalAddress + "\n");
                sb.append("isLinkLocalAddress:\t" + isLinkLocalAddress + "\n");
                sb.append("isLoopbackAddress:\t" + isLoopbackAddress + "\n");
                sb.append("isReachable:\t\t" + isReachable + "\n\n");
            }

            textArea.setText(sb.toString());
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setPreferredSize(new Dimension(640, 480));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to retreive IP-address informations for \"" + serverName + "\"", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } // findIpAddr

    // Log messages to specified log-file
    private static void writeLog(String clientID, String message, File logfile) {
        try (PrintWriter logWriter = new PrintWriter(new FileOutputStream(logfile, true))) {
            Date date = new Date();
            DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
            String formattedMessage = new String(df.format(date) + " | " + clientID + ": " + message + "\n");
            logWriter.append(formattedMessage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to write log-file \"" + logfile.getAbsolutePath() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } // writeLog

    // Create RMI registry with the specified port
    private static void startRegistry() {
        try {
            messageService = new MessageServiceServer();
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            JOptionPane.showMessageDialog(null, "RMI registry successfully created", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to create RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Set GUI elements
        buttonStartRegistry.setEnabled(false);
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonFindIp.setEnabled(true);
        textServerName.setEnabled(true);
        textQueueSize.setEnabled(true);
        textTTL.setEnabled(true);
    } // startRegistry

    // Start a new server and bind it to the RMI registry
    public static void startServer() {

        // Read content of text-fields into static variables
        serverName = textServerName.getText();
        ttl = Integer.parseInt(textTTL.getText());
        deliveryQueueSize = Integer.parseInt(textQueueSize.getText());
        deliveryQueue = new DeliveryQueue(deliveryQueueSize);

        // Install security manager
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // Bind server with the specified name to the local RMI registry
        try {
            registry = LocateRegistry.getRegistry();
            registry.rebind(serverName, messageService);
            JOptionPane.showMessageDialog(null, "Server \"" + serverName + "\" successfully bound to the RMI registry", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to bind server \"" + serverName + "\" to the RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Set GUI elements
        buttonStart.setEnabled(false);
        buttonFindIp.setEnabled(false);
        buttonStop.setEnabled(true);
        textServerName.setEnabled(false);
        textQueueSize.setEnabled(false);
        textTTL.setEnabled(false);
    } // startServer

    // Unbind server from RMI registry
    private static void stopServer() {

        try{
            Naming.unbind(serverName);
            JOptionPane.showMessageDialog(null, "Server \"" + serverName + "\" successfully stopped", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to stop server \"" + serverName + "\"", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Set GUI elements
        buttonStartRegistry.setEnabled(false);
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonFindIp.setEnabled(true);
        textServerName.setEnabled(true);
        textQueueSize.setEnabled(true);
        textTTL.setEnabled(true);
    } // stopServer

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