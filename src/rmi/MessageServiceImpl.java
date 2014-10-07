package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MessageServiceImpl extends UnicastRemoteObject implements MessageService {

    private static final long serialVersionUID = 1L;
    private final int DELIVERYQUEUESIZE = 10; 
    private DeliveryQueue deliveryQueue;
    
    protected MessageServiceImpl() throws RemoteException {
        super();
        deliveryQueue = new DeliveryQueue(DELIVERYQUEUESIZE);
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException { // synchronized, if using threading
        System.out.println("Client hat Methode nextMessage aufgerufen\n");
        Message message = deliveryQueue.getMessage(clientID);        
        return message.toString();
    }

    @Override
    public void newMessage(String clientID, String message) throws RemoteException { // synchronized, if using threading
        System.out.println("Client hat Methode newMessage aufgerufen\n");
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
