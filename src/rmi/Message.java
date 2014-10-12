package rmi;

import java.util.Date;
import java.util.HashMap;

public class Message {

    private static int messageIDCounter = 0;
    private Message nextMessage;
    private String message;
    private String clientID;
    private int messageID;
    private Date date;
    private HashMap<String, Date> visited;
    private final int t = 60;

    Message(String clientID, String message) {
        nextMessage = null;
        this.message = message;
        this.clientID = clientID;
        messageID = ++messageIDCounter;
        date = new Date();
        visited = new HashMap<String, Date>();
    }

    public Message getMessage(String clientID) {
        Date tempDate = new Date();

        if (!visited.containsKey(clientID)) {
            visited.put(clientID, tempDate);
            return this;
        } else {
            if (((tempDate.getTime() - visited.get(clientID).getTime()) / 1000) >= t) {
                visited.remove(clientID);
                return this;
            }
        }

        return null;
    }

    public String getMessageContent() {
        return message;
    }

    public String getClientID() {
        return clientID;
    }

    public int getMessageID() {
        return messageID;
    }

    public Date getDate() {
        return date;
    }

    public Message getNextMessage() {
        return nextMessage;
    }

    public void setNextMessage(Message nextMessage) {
        this.nextMessage = nextMessage;
    }

    @Override
    public String toString() {
        return messageID + " " + clientID + ": " + message + " " + date;
    }

}