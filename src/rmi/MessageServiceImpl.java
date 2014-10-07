package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MessageServiceImpl extends UnicastRemoteObject implements MessageService {

    private static final long serialVersionUID = 1L;
    private DeliveryQueue deliveryQueue;
    
    protected MessageServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException { // synchronized, if using threading
        Message message = deliveryQueue.getMessage(clientID);        
        return message.toString();
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException { // synchronized, if using threading
        Message newMessage = new Message(clientID, message);
        deliveryQueue.addMessage(newMessage);
        System.out.println("Neue Nachricht von Client-ID [" + clientID + "]: " + message + "\n");
    }

    public static void main(String[] args) {

        try {
            MessageService messageService = new MessageServiceImpl();
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("MessageService", messageService);
            System.out.println("MessageService-Server wurde initialisiert!");
        } catch (RemoteException e) {
            System.err.println("Fehler bei der Initialisierung des MessageService-Servers:");
            e.printStackTrace();
        }

    }

}
