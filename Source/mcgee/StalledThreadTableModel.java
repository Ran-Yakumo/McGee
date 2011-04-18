package mcgee;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

class StalledThreadTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1985733360235231431L;
	//Define required variables
    String[] columnNames = {"Board", "Thread Name", "View Thread", "Assigned To Character", "Last Poster", "Second-Last Poster", "Third-Last Poster"};
    public static Vector<StalledThread> threads = new Vector<StalledThread>();

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return threads.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return threads.get(row).getBoard();
        } else if (col == 1) {
            return threads.get(row).getName();
        } else if (col == 2) {
            return false;
        } else if (col == 3) {
            return threads.get(row).getAssigned();
        } else if (col == 4) {
            return threads.get(row).getPoster(0);
        } else if (col == 5) {
            return threads.get(row).getPoster(1);
        } else if (col == 6) {
            return threads.get(row).getPoster(2);
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == 2 || col == 3) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 2) {
            GUI.PrintLineToMainOutput("Opening browser at " + threads.get(row).getLink());
            Utilities.OpenDefaultBrowser(threads.get(row).getLink());
        } else if (col == 3) {
            threads.get(row).setAssigned((String) value);
        }
    }

    public void addStalledThread(StalledThread in) {
        threads.add(in);
        fireTableRowsInserted(threads.size(), threads.size() + 1);
    }

    public String toString() {
        String toReturn = "";
        for (StalledThread current : threads) {
            toReturn += "{" + current.getBoard() + "} (" + current.getName() + ") " + current.getPostersAsString() + "\r\n";
        }
        return toReturn;
    }
}
