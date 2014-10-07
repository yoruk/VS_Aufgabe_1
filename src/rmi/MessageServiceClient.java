package rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MessageServiceClient {

    private MessageService messageService;
    
    public MessageServiceClient() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost"); // Die Registry wird auf "localhost" gesucht
        messageService = (MessageService) registry.lookup("MessageService");
    }

    public void testMethod() throws RemoteException {
        String clientID = "Eugen";
        messageService.newMessage("Hallo Welt!", clientID);
        String message = messageService.nextMessage(clientID);
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        
        try {
            MessageServiceClient messageServiceClient = new MessageServiceClient();
            messageServiceClient.testMethod();
        } catch (NotBoundException e) {
            System.err.println("Das Remote Objekt konnte in der Registry nicht gefunden werden.");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Fehler bei der Kommunikation mit dem MessageService-Server");
            e.printStackTrace();
        }
    }
}
