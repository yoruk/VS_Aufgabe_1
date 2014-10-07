package rmi;

public class DeliveryQueue {

    private Message firstMessage;
    private Message lastMessage;
    private final int maxSize;
    private int size;
            
    DeliveryQueue(int maxSize) {
        this.maxSize = maxSize;
        this.size = 0;  
        this.firstMessage = null;
        this.lastMessage = null;
    }
    
    public void addMessage(Message message) {
        if(firstMessage == null) {
            firstMessage = message;
            lastMessage = message;
            size++;
        } else {
            if(size == maxSize) {
                firstMessage = firstMessage.getNextMessage();
            } else if (size < maxSize) {
                size++;
            }            
            lastMessage.setNextMessage(message);
        }
    }
    
    public Message getMessage(String clientID) {
        Message tempMessage = firstMessage;
        Message tempMessage2;

        if(firstMessage != null) {

            if((tempMessage.getMessage(clientID)) != null) {
                return tempMessage;
            }

            while((tempMessage = tempMessage.getNextMessage()) != null) {

                if((tempMessage2 = tempMessage.getMessage(clientID)) != null) {
                    return tempMessage2;
                }
            }
        } // if
        return null;
    }
}
