package mn.jhn.server;

import java.util.*;

public class MessageQueue
{
                        //     to         from       messages
    private static final Map<String, Map<String, List<String>>> messages;

    static
    {
        messages = Collections.synchronizedMap(new HashMap<String, Map<String, List<String>>>());
    }

    public synchronized static void addOfflineMessage(String receiver, String sender, String message)
    {
        // receiver already has pending messages from people
        if (messages.containsKey(receiver))
        {
            Map<String, List<String>> senderToMessageMap = messages.get(receiver);
            // receiver already has messages form this particular sender
            if (senderToMessageMap.containsKey(sender))
            {
                List<String> previousMessagesFromSender = senderToMessageMap.get(sender);
                previousMessagesFromSender.add(message);
            }
            // create new List for this sender and put the message in
            else
            {
                List<String> messagesFromSender = new ArrayList<String>();
                messagesFromSender.add(message);
                senderToMessageMap.put(sender, messagesFromSender);
                MessageQueue.messages.put(receiver, senderToMessageMap);
            }
        }
        // create new offline message container and put everything in
        else
        {
            List<String> messages = new ArrayList<String>();
            messages.add(message);
            Map<String, List<String>> senderToMessageMap = new HashMap<String, List<String>>();
            senderToMessageMap.put(sender, messages);
            // now add it to the receiver map
            MessageQueue.messages.put(receiver, senderToMessageMap);
        }
    }

    public synchronized static Map<String, List<String>> pendingMessagesForUser(String username)
    {
        return messages.get(username);
    }

    public synchronized static Map<String, List<String>> emptyInboxForUser(String username)
    {
        return messages.remove(username);
    }
}
