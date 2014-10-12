package rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;


public class MessageServiceClientTest {

    public static void main(String[] args) {

        String hostname = "localhost";

        try {
            Registry registry = LocateRegistry.getRegistry();
            MessageService client = (MessageService) registry.lookup(hostname);

            client.newMessage("Tester", "Message [1]");
            client.newMessage("Tester", "Message [2]");
            System.out.println(client.nextMessage("Tester"));
            System.out.println(client.nextMessage("Tester"));
        } catch (NotBoundException e) {
            System.err.println("Der Server " + hostname + " existiert nicht in der RMI Registry!");
            JOptionPane.showMessageDialog(null, "Der Server \"" + hostname + "\" existiert nicht in der RMI Registry!", "FEHLER", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Der Server " + hostname + " antwortet nicht!");
            JOptionPane.showMessageDialog(null, "Der Server \"" + hostname + "\" antwortet nicht!", "FEHLER", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}