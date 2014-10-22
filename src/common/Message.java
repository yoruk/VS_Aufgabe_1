package common;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Message {

    /* Class variables */
    private final DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMANY); // Time formatter
    private HashMap<String, Date> clientsRequestsList; // List with clients requests saved as a pair of client-ID and timestamp
    private static int messageIDCounter = 1; // Static counter for messages
    private Message nextMessage;             // Reference to next message
    private String message;                  // Message content
    private String clientID;                 // Client-ID
    private int messageID;                   // Continuous message-ID
    private int msgResendTime;               // Message-Resend-Time for all messages and clients
    private Date date;                       // Date object for time measurements

    /* Constructor */
    Message(String clientID, String message, int msgResendTime) {
        this.clientID = clientID;
        this.message = message;
        this.msgResendTime = msgResendTime;
        nextMessage = null;
        messageID = ++messageIDCounter;
        date = new Date();
        clientsRequestsList = new HashMap<String, Date>();
    }

    /* This method returns a message object only if the specified client doesn't request it already or the request is past the message-resend-time */
    public Message getValidMessage(String clientID) {
        Date tempDate = new Date();
        Message tempMessage = null;

        if (!clientsRequestsList.containsKey(clientID)) {
            clientsRequestsList.put(clientID, tempDate); // Request from unknown client-ID: Put new client-ID and current request timestamp into HashMap
            tempMessage = this;
        } else {
            if (((tempDate.getTime() - clientsRequestsList.get(clientID).getTime()) / 1000) >= msgResendTime) {
                clientsRequestsList.remove(clientID);        // Request of client-ID is past the message-resend-time, so delete old entry and
                clientsRequestsList.put(clientID, tempDate); // refresh HashMap with a new entry: Put same client-ID but new request timestamp into HashMap
                tempMessage = this;
            }
        }

        return tempMessage;
    }

    /* Getter and setter */
    public Message getNextMessage() {
        return nextMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getClientID() {
        return clientID;
    }

    public int getMessageID() {
        return messageID;
    }

    public int getMsgResendTime() {
        return msgResendTime;
    }

    public Date getDate() {
        return date;
    }

    public void setNextMessage(Message nextMessage) {
        this.nextMessage = nextMessage;
    }

    @Override
    public String toString() {
        return "[" + (messageID - 1) + "] " + clientID + ": " + message + " (" + new String(df.format(date)) + ")";
    } /* toString */

}