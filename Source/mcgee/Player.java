package mcgee;

import java.util.HashSet;

//This keeps all the relevant data on a player
public class Player implements Comparable<Player> {

    // Working private members
    private String main;
    private String alt;
    private int threads;
    private int strikes;
    private int goodScans;
    private boolean onHiatus;
    private int hiatusWeeksRemaining;
    private String lastPostTime;
    private String lastPostThread;
    private String lastPostBoard;
    private HashSet<StalledThread> assigned = new HashSet<StalledThread>();
    private HashSet<BadPost> shortPosts = new HashSet<BadPost>();
    private HashSet<BadPost> longPosts = new HashSet<BadPost>();
    private boolean expiredHiatus = false;
    private int index = -1;

    public Player(String inMain, String inAlt, int inThreads, String inLastPostTime, String inLastPostThread,
            String inLastPostBoard, int inStrikes, int inGoodScans, boolean inOnHiatus, int inHiatusWeeksRemaining) {
        main = inMain;
        alt = inAlt;
        threads = inThreads;
        lastPostTime = inLastPostTime;
        lastPostThread = inLastPostThread;
        lastPostBoard = inLastPostBoard;
        strikes = inStrikes;
        goodScans = inGoodScans;
        onHiatus = inOnHiatus;
        hiatusWeeksRemaining = inHiatusWeeksRemaining;
    }

    public String getMain() {
        return main;
    }

    public String getAlt() {
        return alt;
    }

    public int getThreads() {
        return threads;
    }

    public String getLastPostTime() {
        return lastPostTime;
    }

    public String getLastPostThread() {
        return lastPostThread;
    }

    public String getLastPostBoard() {
        return lastPostBoard;
    }

    public int getStalledThreads() {
        return assigned.size();
    }

    public int getStrikes() {
        int toReturn = strikes;
        if (assigned.size() > 0) {
            toReturn += 1;
        }
        if (assigned.size() > 2) {
            toReturn += 1;
        }
        if (Main.scanDone && assigned.isEmpty() && getGoodScans() % 2 == 0 && toReturn > 0 && !onHiatus) {
            toReturn -= 1;
        }
        return toReturn;
    }

    public int getGoodScans() {
        int toReturn = goodScans;
        if (!assigned.isEmpty()) {
            return 0;
        } else if (Main.scanDone && !onHiatus) {
            toReturn += 1;
        }
        return toReturn;
    }

    public boolean getOnHiatus() {
        return onHiatus;
    }

    public int getHiatusWeeksRemaining() {
        return hiatusWeeksRemaining;
    }

    public HashSet<StalledThread> getAssigned() {
        return assigned;
    }

    public boolean getExpiredHiatus() {
        return expiredHiatus;
    }

    public HashSet<BadPost> getShortPosts() {
        return shortPosts;
    }

    public HashSet<BadPost> getLongPosts() {
        return longPosts;
    }

    public int getIndex() {
        return index;
    }

    public void setMain(String in) {
        main = in;
    }

    public void setAlt(String in) {
        alt = in;
    }

    public void setThreads(int inThreads) {
        threads = inThreads;
    }

    public void incrementThreads() {
        threads++;
    }

    public void setLastPostTime(String inDate) {
        lastPostTime = inDate;
    }

    public void setLastPostThread(String inThread) {
        lastPostThread = inThread;
    }

    public void setLastPostBoard(String inBoard) {
        lastPostBoard = inBoard;
    }

    public void setStrikes(int in) {
        strikes = in;
        if (assigned.size() > 0) {
            strikes -= 1;
        }
        if (assigned.size() > 2) {
            strikes -= 1;
        }
        if (Main.scanDone && assigned.isEmpty() && getGoodScans() % 2 == 0 && strikes > 0) {
            strikes += 1;
        }
    }

    public void setGoodScans(int in) {
        goodScans = in;
        if (Main.scanDone && assigned.isEmpty()) {
            goodScans -= 1;
        }
    }

    public void setOnHiatus(boolean inOnHiatus) {
        onHiatus = inOnHiatus;
    }

    public void setHiatusWeeksRemaining(int inHiatusWeeksRemaining) {
        hiatusWeeksRemaining = inHiatusWeeksRemaining;
    }

    public void decrementHiatusWeeksRemaining() {
        hiatusWeeksRemaining--;
    }

    public void setExpiredHiatus(boolean in) {
        expiredHiatus = in;
    }

    public void addShortPost(BadPost in) {
        shortPosts.add(in);
    }

    public void addLongPost(BadPost in) {
        longPosts.add(in);
    }

    public void setIndex(int in) {
        index = in;
    }

    public Object getValue(int col) {
        if (col == 0) {
            return getMain();
        } else if (col == 1) {
            return getAlt();
        } else if (col == 2) {
            return getThreads();
        } else if (col == 3) {
            return getLastPostTime();
        } else if (col == 4) {
            return getLastPostThread();
        } else if (col == 5) {
            return getLastPostBoard();
        } else if (col == 6) {
            return getStalledThreads();
        } else if (col == 7) {
            return getStrikes();
        } else if (col == 8) {
            return getGoodScans();
        } else if (col == 9) {
            return getOnHiatus();
        } else if (col == 10) {
            return getHiatusWeeksRemaining();
        }
        return null;
    }

    public void setValue(int col, Object in) {
        if (col == 0) {
            setMain((String) in);
        } else if (col == 1) {
            setAlt((String) in);
        } else if (col == 2) {
            setThreads((Integer) in);
        } else if (col > 2 && col < 7) {
            return;
        } else if (col == 7) {
            setStrikes((Integer) in);
        } else if (col == 8) {
            setGoodScans((Integer) in);
        } else if (col == 9) {
            setOnHiatus((Boolean) in);
        } else if (col == 10) {
            setHiatusWeeksRemaining((Integer) in);
        }
    }

    // Removes an assigned thread from a player
    public void removeThread(StalledThread in) {
        // Remove the stalled thread from the set of threads assigned to this
        // player
        assigned.remove(in);

        // Update the table with stalled threads, strikes, and consecutive good
        // scans
        GUI.playerTableModel.fireTableCellUpdated(index, 6);
        GUI.playerTableModel.fireTableCellUpdated(index, 7);
        GUI.playerTableModel.fireTableCellUpdated(index, 8);
    }

    // Gives a strike to a player and takes care of all ramifications of doing
    // so.
    public void assignThread(StalledThread in) {
        // Add this thread to the list of assigned threads for this player
        assigned.add(in);

        // Update the table with stalled threads, strikes, and consecutive good
        // scans
        GUI.playerTableModel.fireTableCellUpdated(index, 6);
        GUI.playerTableModel.fireTableCellUpdated(index, 7);
        GUI.playerTableModel.fireTableCellUpdated(index, 8);
    }

    // Updates the Last IC Post data for this player
    public void updateLastPost(String inTime, String inThread, String inBoard) {
        if (Utilities.ParseDateString(inTime) > Utilities.ParseDateString(lastPostTime)) {
            lastPostTime = inTime;
            lastPostThread = inThread;
            lastPostBoard = inBoard;
            GUI.playerTableModel.fireTableCellUpdated(index, 3);
            GUI.playerTableModel.fireTableCellUpdated(index, 4);
            GUI.playerTableModel.fireTableCellUpdated(index, 5);
        }
    }

    public int compareTo(Player other) {
        return this.main.compareTo(other.getMain());
    }
}
