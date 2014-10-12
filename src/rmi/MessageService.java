package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageService extends Remote {

    /**
     * 
     * @param clientID
     * @return Undelivered messages from the delivery-queue to the specified client-ID
     * @throws RemoteException
     */
    public String nextMessage(String clientID) throws RemoteException;

    /**
     * 
     * @param clientID
     * @param message
     * @throws RemoteException
     */
    public void newMessage(String clientID, String message) throws RemoteException;

}
