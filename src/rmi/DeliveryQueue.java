package rmi;

public class DeliveryQueue {

    /* Class variables */
    private Message firstMessage; // Beginning of the FIFO queue
    private final int maxSize;    // Maximum size of the FIFO queue
    private int currentSize;      // Current queue size

    /* Constructor */
    DeliveryQueue(int maxSize) {
        this.firstMessage = null;
        this.maxSize = maxSize;
        this.currentSize = 0;
    }

    /* This method adds a new message into the FIFO queue */
    public void addMessage(Message message) {
        Message tempMessage = firstMessage;

        if (firstMessage == null) {
            firstMessage = message; // Empty FIFO queue: Attach "message" to first position of the FIFO queue
            currentSize++;
        } else {
            while (tempMessage.getNextMessage() != null) {
                tempMessage = tempMessage.getNextMessage(); // FIFO queue not empty: Search for the last message
            }

            tempMessage.setNextMessage(message); // Attach "message" to the last message in the FIFO queue

            if (currentSize == maxSize) {
                firstMessage = firstMessage.getNextMessage(); // Maximum size of FIFO queue reached: Discard first message entry
            } else {
                currentSize++;
            }
        }
    } /* addMessage */

    public Message getMessage(String clientID) {
        Message tempMessage = firstMessage;
        Message tempMessage2;

        if (firstMessage != null) {
            if ((tempMessage.getValidMessage(clientID)) != null) {
                return tempMessage;
            }

            while ((tempMessage = tempMessage.getNextMessage()) != null) {
                if ((tempMessage2 = tempMessage.getValidMessage(clientID)) != null) {
                    return tempMessage2;
                }
            }
        }

        return null;
    } /* getMessage */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\n**********     MESSAGE QUEUE     **********\n");
        Message tempMessage = firstMessage;

        if (tempMessage != null) {
            sb.append(tempMessage).append("\n");

            while (tempMessage.getNextMessage() != null) {
                tempMessage = tempMessage.getNextMessage();
                sb.append(tempMessage).append("\n");
            }
        } else {
            sb.append("          The message queue is empty!          \n");
        }

        sb.append("***********************************************\n\n");

        return sb.toString();
    } /* toString */

}