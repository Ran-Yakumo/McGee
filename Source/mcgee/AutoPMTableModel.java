package mcgee;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

class AutoPMTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4173653588807402428L;
	String[] columnNames = {"Recipient", "Subject", "Edit Message", "Send PM", "Status"};
    public static ArrayList<AutoPM> PMs = new ArrayList<AutoPM>();

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return PMs.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return PMs.get(row).getRecipient().getMain();
        }
        else if (col == 1) {
            return PMs.get(row).getSubject();
        }
        else if (col == 2) {
            return false;
        }
        else if (col == 3) {
            return PMs.get(row).getSend();
        }
        else {
            return PMs.get(row).getSent();
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == 0 || col == 4) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 1) {
            PMs.get(row).setSubject((String) value);
            fireTableCellUpdated(row, col);
        }
        else if (col == 2) {
            GUI.messageEditDialog(row);
        }
        else if (col == 3) {
            PMs.get(row).setSend((Boolean) value);
            fireTableCellUpdated(row, col);
        }
    }

    //Clears the current table of all PM's
    public void clearAll() {
        PMs.clear();
    }

    //Adds a PM to the table
    public void addPM(AutoPM in) {
        PMs.add(in);
    }

    //Attempts to send all the PM's that are marked for sending
    public void sendPM() {
        for (int i = 0; i < PMs.size(); i++) {
            AutoPM current = PMs.get(i);
            if (current.getSend()) {
                //Do final dynamic replacements right before sending.
                current.setMessage(current.getMessage().replaceAll("%activity", current.buildActivityMessage()));

                //Replace all "[" and "]", they are apparently illegal characters
                current.setMessage(current.getMessage().replaceAll("\\[", "("));
                current.setMessage(current.getMessage().replaceAll("\\]", ")"));

                //Send the message
                if (Utilities.SendAutomatedPM(current)) {
                    current.setSent("Success");
                } else {
                    current.setSent("Failed");
                }
                fireTableCellUpdated(i, 4);
            }
        }
    }
}
