package mcgee;

public class AutoPM {

    //AutoPM data such as default messages and the auth data for sending PM's
    public static String AutoPMUser;
    public static String AutoPMPass;
    private static String DefaultMessage = "";
    private static String DefaultActivityStrikeMessage = "";
    private static String DefaultStalledThreadForm = "";
    private static String DefaultStrikeRemovedMessage = "";
    private static String DefaultExpiredHiatusMessage = "";
    private static String DefaultShortPostMessage = "";
    private static String DefaultShortPostForm = "";
    private static String DefaultLongPostMessage = "";
    private static String DefaultLongPostForm = "";

    public static void setAutoPMUser(String in) {
        AutoPMUser = in;
    }
    public static void setAutoPMPass(String in) {
        AutoPMPass = in;
    }
    public static void setDefaultMessage(String in) {
        DefaultMessage = in;
    }
    public static void setDefaultActivityStrikeMessage(String in) {
        DefaultActivityStrikeMessage = in;
    }
    public static void setDefaultStalledThreadForm (String in) {
        DefaultStalledThreadForm = in;
    }
    public static void setDefaultStrikeRemovedMessage(String in) {
        DefaultStrikeRemovedMessage = in;
    }
    public static void setDefaultExpiredHiatusMessage(String in) {
        DefaultExpiredHiatusMessage = in;
    }
    public static void setDefaultShortPostMessage(String in) {
        DefaultShortPostMessage = in;
    }
    public static void setDefaultShortPostForm(String in) {
        DefaultShortPostForm = in;
    }
    public static void setDefaultLongPostMessage(String in) {
        DefaultLongPostMessage = in;
    }
    public static void setDefaultLongPostForm(String in) {
        DefaultLongPostForm = in;
    }

    //Member variables
    private Player recipient;
    private String subject;
    private String message;
    private boolean send = false;
    private String sent = "Not Attmpted";
    
    public AutoPM(Player inRecipient, String inSubject) {
        recipient = inRecipient;
        subject = inSubject;
        
        //Initialize message to be the default message
        message = DefaultMessage;

        //replace everythingexcept for the activity message, which has to be generated dynamically at the time of sending.
        message = message.replaceAll("%expired", buildExpiredHiatusMessage());
        message = message.replaceAll("%short", buildShortPostMessage());
        message = message.replaceAll("%long", buildLongPostMessage());
    }

    public void setRecipient(Player inRecipient) {
        recipient = inRecipient;
    }

    public void setSubject(String inSubject) {
        subject = inSubject;
    }

    public void setMessage(String inMessage) {
        message = inMessage;
    }

    public void setSend(boolean inSend) {
        send = inSend;
    }

    public void setSent(String inSent) {
        sent = inSent;
    }

    public Player getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public boolean getSend() {
        return send;
    }

    public String getSent() {
        return sent;
    }

    public String buildActivityMessage() {
        String toReturn = "";
        if (recipient.getAssigned().isEmpty()) {
            //If this player is having a strike removed, use the strike removed message
            if (recipient.getGoodScans() % 2 == 0 && recipient.getStrikes() > 0 && !recipient.getOnHiatus()) {
                toReturn = DefaultStrikeRemovedMessage.replaceAll("%username", recipient.getMain());
                toReturn = toReturn.replaceAll("%strikes", "" + recipient.getStrikes());
                return toReturn.replaceAll("%scans", "" + recipient.getGoodScans());
            }
            //If this user has nothing to do for activity, return an empty string.
            else {
                return "";
            }
        }
        //If this player has been assigned stalled threads, output a proper strike message
        else {
            //Perform static replacements
            toReturn = DefaultActivityStrikeMessage.replace("%username", recipient.getMain());
            toReturn = toReturn.replaceAll("%strikes", "" + recipient.getStrikes());
            if (recipient.getAssigned().size() < 3) {
                toReturn = toReturn.replaceAll("%added", "" + 1);
            } else {
                toReturn = toReturn.replaceAll("%added", "" + 2);
            }

            //Perform dynamic replacements
            String threads = "";
            for (StalledThread current : recipient.getAssigned()) {
                String toAdd = DefaultStalledThreadForm.replaceAll("%board", current.getBoard());
                threads += toAdd.replaceAll("%thread", current.getName());
            }
            return toReturn.replaceAll("%threads", threads);
        }
    }

    public final String buildExpiredHiatusMessage() {
        //If this player's hiatus is not expired, do nothing.
        if (!recipient.getExpiredHiatus()) {
            return "";
        }
        //Only one replacement to do, "%username" -> player's main
        return DefaultExpiredHiatusMessage.replaceAll("%username", recipient.getMain());
    }

    public final String buildShortPostMessage() {
        //If this player made no short posts, do nothing.
        if (recipient.getShortPosts().isEmpty()) {
            return "";
        }
        
        //Set up the defualt message
        String toReturn = DefaultShortPostMessage.replace("%username", recipient.getMain());

        //Build up the list of short posts
        String posts = "";
        for (BadPost current : recipient.getShortPosts()) {
            String toAdd = DefaultShortPostForm.replaceAll("%board", current.getBoard());
            toAdd = toAdd.replaceAll("%thread", current.getThread());
            posts += toAdd.replaceAll("%words", "" + current.getWordCount());
        }

        //Do final replacements and return new message
        return toReturn.replaceAll("%posts", posts);
    }

    public final String buildLongPostMessage() {
        //If this player made no short posts, do nothing.
        if (recipient.getLongPosts().isEmpty()) {
            return "";
        }

        //Set up the defualt message
        String toReturn = DefaultLongPostMessage.replace("%username", recipient.getMain());

        //Build up the list of short posts
        String posts = "";
        for (BadPost current : recipient.getShortPosts()) {
            String toAdd = DefaultLongPostForm.replaceAll("%board", current.getBoard());
            toAdd = toAdd.replaceAll("%thread", current.getThread());
            posts += toAdd.replaceAll("%words", "" + current.getWordCount());
        }

        //Do final replacements and return new message
        return toReturn.replaceAll("%posts", posts);
    }
}
