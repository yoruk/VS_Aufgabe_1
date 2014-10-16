package rmi;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Message {

    private static int messageIDCounter = 0;
    private Message nextMessage;
    private String message;
    private String clientID;
    private int messageID;
    private int ttl; // Message time
    private Date date;
    private final DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY);
    private HashMap<String, Date> visited;

    Message(String clientID, String message, int ttl) {
        nextMessage = null;
        this.message = message;
        this.clientID = clientID;
        this.ttl = ttl;
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
            if (((tempDate.getTime() - visited.get(clientID).getTime()) / 1000) >= ttl) {
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
        return "[" + messageID + "] " + clientID + ": " + message + " (" + new String(df.format(date)) + ")";
    }

}