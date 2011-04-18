package mcgee;

import java.util.LinkedList;

public class StalledThread {

    private String board;
    private String name;
    private String link;
    private String assigned = "Not Assigned";
    private LinkedList<String> posters;

    public StalledThread(String inBoard, String inName, String inLink, LinkedList<String> inPosters) {
        board = inBoard;
        name = inName;
        link = inLink;
        posters = inPosters;
    }

    public String getBoard() {
        return board;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getAssigned() {
        return assigned;
    }

    public String getPoster(int in) {
        if (in < posters.size()) {
            return posters.get(posters.size() - (in + 1));
        }
        else {
            return "None";
        }
    }

    public String getPostersAsString() {
        String toReturn = "";
        for (String current : posters) {
            toReturn += current + " ";
        }
        return toReturn;
    }

    public void setAssigned(String in) {
        //If any player is currently assigned this thread, break the association
        if (GUI.playerTableModel.containsPlayer(assigned)) {
            GUI.playerTableModel.getPlayerByName(assigned).removeThread(this);
            assigned = "Not Assigned";
        }
        //Form an association with this player if possible
        if (GUI.playerTableModel.containsPlayer(in)) {
            if (GUI.playerTableModel.getPlayerByName(in).getOnHiatus()) {
                GUI.PrintLineToMainOutput("Player " + in + " is on hiatus. Not asigning stalled thread.");
            }
            else {
                assigned = in;
                GUI.playerTableModel.getPlayerByName(assigned).assignThread(this);
            }
        } else {
            GUI.PrintLineToMainOutput("Player " + in + " could not be found. Did you spell her name properly?");
        }
    }
}
