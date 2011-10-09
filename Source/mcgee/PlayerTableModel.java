package mcgee;

import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

class PlayerTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 7559688232320866013L;
    String[] columnNames = { "Main", "Alt", "Active Threads", "Last IC Post Time", "Last IC Post Thread",
            "Last IC Post Board", "Stalled Threads", "Strikes", "Consecutive Scans Passed", "On Hiatus",
            "Hiatus Weeks Remaining" };
    private static ArrayList<Player> players = new ArrayList<Player>();
    private static HashMap<String, Player> mainMap = new HashMap<String, Player>();
    private static HashMap<String, Player> altMap = new HashMap<String, Player>();

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return players.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return players.get(row).getValue(col);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col < 3 || col > 6) {
            return true;
        }
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // If this is the main or alt, update the mapping appropriately
        if (col == 0) {
            mainMap.remove(players.get(row).getMain());
            mainMap.put((String) value, players.get(row));
        } else if (col == 1) {
            altMap.remove(players.get(row).getAlt());
            altMap.put((String) value, players.get(row));
        }

        players.get(row).setValue(col, value);
        fireTableCellUpdated(row, col);
    }

    // Says whether a certain player is in the table or not
    public boolean containsPlayer(String in) {
        // Check to be sure that this is not someone testing for "None"
        if (in.equals("None")) {
            return false;
        }

        // Get the name of the player's main and test the name map
        if (mainMap.containsKey(in) || altMap.containsKey(in)) {
            return true;
        }

        // Player was not found, return false
        return false;
    }

    // Gets the player using character name
    public Player getPlayerByName(String in) {
        // Get the player object from one of the maps
        if (mainMap.containsKey(in)) {
            return mainMap.get(in);
        } else if (altMap.containsKey(in)) {
            return altMap.get(in);
        } else {
            return null;
        }
    }

    // Puts a new player into the table
    public void addPlayer(Player toAdd) {

        // Update the mappings and create the player object
        mainMap.put(toAdd.getMain(), toAdd);
        altMap.put(toAdd.getAlt(), toAdd);

        // Insert the player into the table
        players.add(toAdd);
        Collections.sort(players);

        // Update all the indices
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setIndex(i);
        }

        // Fire the row inserted event to ensure that the player appears in the
        // table
        fireTableRowsInserted(toAdd.getIndex(), toAdd.getIndex() + 1);
    }

    // Removes a player from the table
    public void dropPlayer(Player toDrop) {
        // Update the mappings
        mainMap.remove(toDrop.getMain());
        altMap.remove(toDrop.getAlt());

        // Update the table
        players.remove(toDrop);
        Collections.sort(players);

        // Update all the indices
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setIndex(i);
        }

        // Fire the row deleted event to ensure that the player disappears in
        // the table
        fireTableRowsDeleted(toDrop.getIndex(), toDrop.getIndex() + 1);
    }

    // Prepares for a scan.
    public void readyForScan() {
        for (Player current : players) {
            // Prepares the thread counts
            current.setThreads(0);

            // If this person is on hiatus, make sure they have some hiatus time
            // left, and excuse them from the scan if they do
            if (current.getOnHiatus()) {
                if (current.getHiatusWeeksRemaining() > 0) {
                    current.decrementHiatusWeeksRemaining();
                } else {
                    current.setOnHiatus(false);
                    current.setExpiredHiatus(true);
                    Main.ExpiredHiatus.add(current.getMain());
                }
            }
        }
    }

    // Finishes a scan.
    public void finishScan() {
        // Update everythign in the table, many things changed during the scan
        fireTableDataChanged();

        // Generate all AutoPM's
        for (Player current : players) {
            if (!current.getOnHiatus()) {
                GUI.PMTableModel.addPM(new AutoPM(current, "Activity Scan Results"));
            }
        }
    }

    // Dumps the current table into a file for safekeeping
    public void DumpPlayerFile() {
        try {
            // Allocate resources
            FileWriter outFile = new FileWriter("players.txt");
            outFile.write(Main.LastScanTime + "\r\n");
            outFile.write(Main.CurrentTime + "\r\n");

            // Output the current table to a file
            for (Player current : players) {
                // Output this player's parameters to the file
                outFile.write(current.getMain() + "\r\n");
                outFile.write(current.getAlt() + "\r\n");
                outFile.write(current.getThreads() + "\r\n");
                outFile.write(current.getLastPostTime() + "\r\n");
                outFile.write(current.getLastPostThread() + "\r\n");
                outFile.write(current.getLastPostBoard() + "\r\n");
                outFile.write(current.getStrikes() + "\r\n");
                outFile.write(current.getGoodScans() + "\r\n");
                outFile.write(current.getOnHiatus() + "\r\n");
                outFile.write(current.getHiatusWeeksRemaining() + "\r\n");
            }

            // Deallocate resources
            outFile.flush();
            outFile.close();
        } catch (Exception e) {
            System.err.println("Crash in DumpPlayerFile:");
            e.printStackTrace();
        }
    }

    public void DumpInactivePlayers(FileWriter OutFile) {
        try {
            // Dump the names of all offening players
            OutFile.write("\r\nPlayers Who Have not Posted in Two Weeks or More:\r\n");
            for (Player current : players) {
                if (Main.CurrentTime - Utilities.ParseDateString(current.getLastPostTime()) > 20160) {
                    OutFile.write(current.getMain() + "\r\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Crash in PlayerTableModel's DumpInactivePlayers");
            e.printStackTrace();
        }
    }
}
