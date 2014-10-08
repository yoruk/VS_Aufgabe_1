package rmi;

public class DeliveryQueue {

    private Message firstMessage;
    private final int maxSize;
    private int size;
            
    DeliveryQueue(int maxSize) {
        this.maxSize = maxSize;
        this.size = 0;  
        this.firstMessage = null;
    }
    
    public void addMessage(Message message) {
    	Message tempMessage = firstMessage;
    	
    	if(firstMessage == null) {
            firstMessage = message;
            size++;
        } else {            
        	while(tempMessage.getNextMessage() != null) {
				tempMessage = tempMessage.getNextMessage();
			}
        	
        	tempMessage.setNextMessage(message);
            
        	if(size == maxSize) {
        		firstMessage = firstMessage.getNextMessage();
        	} else {
        		size++;
        	}        	
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Message tempMessage = firstMessage;
		
		if(tempMessage != null) {
			sb.append(tempMessage).append("\n");
			
			while(tempMessage.getNextMessage() != null) {
				tempMessage = tempMessage.getNextMessage();
				sb.append(tempMessage).append("\n");
			}
		
		}
		
		return sb.toString();
	}
    
    
}
