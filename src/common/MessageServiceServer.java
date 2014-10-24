package common;

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
import java.io.PrintWriter;
import java.net.InetAddress;
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

    /* Class variables */
    private static final long serialVersionUID = 1L;
    private static final File logSend = new File("server_sent_msgs.txt"); // Logfile for sent messages
    private static final File logRecv = new File("server_recv_msgs.txt"); // Logfile for received messages
    private static StringBuilder stringBuffer = new StringBuilder();      // Buffer for all messages to be displayed in the server GUI
    private static MessageService messageService; // MessageService object for remote connection to the RMI registry
    private static DeliveryQueue deliveryQueue;   // Delivery queue object to handle the incoming and outgoing messages
    private static Registry registry;             // RMI registry to bind the server to
    private static String serverName;             // Name for the server to register in the RMI registry
    private static int msgResendTime;             // Time between messages being resent to the same client
    private static int deliveryQueueSize;         // Size of the delivery queue
    private static JTextField textServerName;     // Text field for the server name
    private static JTextField textQueueSize;      // Text field for the queue size
    private static JTextField textMsgResendTime;  // Text field the message-repond-time
    private static JTextArea textOutputArea;      // Text area for displaying all messages and the delivery queue
    private static JButton buttonStartRegistry;   // Button to start the RMI registry
    private static JButton buttonStartServer;     // Button to start the server
    private static JButton buttonShowQueue;       // Button to show the contents of the delivery queue

    /* Constructor */
    public MessageServiceServer() throws RemoteException {
        super();
    }


    /*vvvvvvvvvv     INTERFACE METHODS IMPLEMENTATION     vvvvvvvvvv*/

    @Override
    public String nextMessage(String clientID) {
        Message nextMessage = deliveryQueue.getMessage(clientID);
        stringBuffer.append("<< nextMessage call by " + clientID + ": \"" + ((nextMessage == null) ? null : nextMessage.toString()) + "\"").append("\n");
        textOutputArea.setText(stringBuffer.toString());
        writeLog(clientID, ((nextMessage == null) ? null : nextMessage.toString()), logSend);
        return ((nextMessage == null) ? null : nextMessage.toString());
    } /* nextMessage */

    @Override
    public void newMessage(String clientID, String message) {
        Message newMessage = new Message(clientID, message, msgResendTime);
        stringBuffer.append(">> newMessage call by " + clientID + ": \"" + message + "\"").append("\n");
        textOutputArea.setText(stringBuffer.toString() + "\n");
        writeLog(clientID, newMessage.toString(), logRecv);
        deliveryQueue.addMessage(newMessage);
    } /* newMessage */

    /*^^^^^^^^^^     INTERFACE METHODS IMPLEMENTATION     ^^^^^^^^^^*/


    /* This method creates and shows the GUI window */
    private static void createAndShowGUI() {

        JFrame frame = new JFrame("MessageService Server");          // Window name                               
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        // Operation when "close" button is clicked  
        frame.setIconImage(frame.getToolkit().getImage("icon.png")); // Application icon                          
        frame.setMinimumSize(new Dimension(800, 600));               // Window size                               
        frame.setResizable(false);                                   // Window resizing                         

        // Create top panel
        JPanel panelTop = new JPanel(new GridLayout(5, 2));
        panelTop.setBorder(BorderFactory.createEtchedBorder(1));
        JLabel labelServerName = new JLabel("Server name:");
        textServerName = new JTextField("MessageService");
        textServerName.setEnabled(false);
        JLabel labelQueueSize = new JLabel("Queue-Size:");
        textQueueSize = new JTextField("10");
        textQueueSize.setEnabled(false);
        JLabel labelMsgResendTime = new JLabel("Message-Resend-Time (in seconds):");
        textMsgResendTime = new JTextField("60");
        textMsgResendTime.setEnabled(false);
        buttonStartRegistry = new JButton("Create RMI registry");
        buttonStartServer = new JButton("Start Server");
        buttonStartServer.setEnabled(false);
        JButton buttonFindIp = new JButton("Find IP-addresses");
        buttonShowQueue = new JButton("Show MessageQueue");
        buttonShowQueue.setEnabled(false);
        panelTop.add(labelServerName);
        panelTop.add(textServerName);
        panelTop.add(labelQueueSize);
        panelTop.add(textQueueSize);
        panelTop.add(labelMsgResendTime);
        panelTop.add(textMsgResendTime);
        panelTop.add(buttonStartRegistry);
        panelTop.add(buttonFindIp);
        panelTop.add(buttonStartServer);
        panelTop.add(buttonShowQueue);

        // Create center panel
        JPanel panelCenter = new JPanel(new FlowLayout());
        panelCenter.setBorder(BorderFactory.createEtchedBorder(1));
        textOutputArea = new JTextArea(32, 76);
        textOutputArea.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(1, 3, 1, 1)));
        textOutputArea.setEditable(false);
        textOutputArea.setForeground(Color.RED);
        textOutputArea.setBackground(Color.WHITE);
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

        buttonStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        buttonFindIp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findIpAddr();
            }
        });

        buttonShowQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeliveryQueue();
            }
        });

        // Add panels to the frame and make it visible
        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.validate();
        frame.setVisible(true);
    } /* createAndShowGUI */

    /* This method shows the contents of the delivery queue */
    private static void showDeliveryQueue() {
        stringBuffer.append(deliveryQueue).append("\n");
        textOutputArea.setText(stringBuffer.toString());
    } /* showDeliveryQueue */

    /* This method searches for all possible local IP-addresses and displays them */
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
    } /* findIpAddr */

    /* This method is saving a given string message to a logfile */
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
    } /* writeLog */

    /* This method creates and starts the RMI registry */
    private static void startRegistry() {

        try {
            messageService = new MessageServiceServer();
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            JOptionPane.showMessageDialog(null, "RMI registry successfully created", "Information", JOptionPane.INFORMATION_MESSAGE);

            // Set GUI components
            buttonStartRegistry.setEnabled(false);
            buttonStartServer.setEnabled(true);
            textServerName.setEnabled(true);
            textQueueSize.setEnabled(true);
            textMsgResendTime.setEnabled(true);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Unable to create RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } /* startRegistry */

    /* This method starts and binds the server to the local RMI registry */
    private static void startServer() {

        // Read content of text-fields into static variables
        serverName = textServerName.getText();
        msgResendTime = Integer.parseInt(textMsgResendTime.getText());
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

            // Set GUI components
            buttonStartServer.setEnabled(false);
            buttonShowQueue.setEnabled(true);
            textServerName.setEnabled(false);
            textQueueSize.setEnabled(false);
            textMsgResendTime.setEnabled(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to bind server \"" + serverName + "\" to the RMI registry", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } /* startServer */


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