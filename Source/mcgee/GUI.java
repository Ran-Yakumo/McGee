package mcgee;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JMenuBar;

public class GUI {

    public static PlayerTableModel playerTableModel = new PlayerTableModel();
    public static AutoPMTableModel PMTableModel = new AutoPMTableModel();
    public static StalledThreadTableModel stalledThreadTableModel = new StalledThreadTableModel();
    private static JTextArea outputArea = new JTextArea();
    private static JLabel statusLabel;

    // Sets up the meme window
    public static void InitializeGUI() {

        // Initialize the Frame
        final JFrame frame = new JFrame("McGee, Keeping Activity Under Control");
        JPanel panel;
        JLabel label;
        JScrollPane scrollPane;
        JTable table;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        WindowListener close = new WindowListener() {
            public void windowClosed(WindowEvent e) {
                saveDialog();
            }

            public void windowDeactivated(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
            }

            public void windowOpened(WindowEvent e) {
            }
        };
        frame.addWindowListener(close);
        Container content = frame.getContentPane();

        // Make the output scroll pane at the bottom of the window
        outputArea.setRows(6);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setAutoscrolls(true);
        outputArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane = new JScrollPane(outputArea);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Create the status bar above the output window
        JPanel statusBarPanel = new JPanel();
        statusBarPanel.setLayout(new BoxLayout(statusBarPanel, BoxLayout.PAGE_AXIS));
        statusLabel = new JLabel("Viewing Nothing");
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusBarPanel.add(statusPanel);
        panel.add(statusBarPanel);
        label = new JLabel("Text Output From Main Program:");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(scrollPane);
        content.add(panel, BorderLayout.SOUTH);

        // Create a button for doing a scan and add it to the menu bar
        JMenuBar menuBar = new JMenuBar();
        JButton scanButton = new JButton("Run Scan");
        ActionListener scan = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utilities.CopyFile("players.txt", "players.bak");
                playerTableModel.readyForScan();
                PMTableModel.clearAll();
                Main.DoScan();
                playerTableModel.finishScan();
            }
        };
        scanButton.addActionListener(scan);
        menuBar.add(scanButton);

        // Create a button for bringing up the PM dialog
        JButton PMButton = new JButton("Open Automatic PM System");
        ActionListener PM = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GUI.PMDialog();
            }
        };
        PMButton.addActionListener(PM);
        menuBar.add(PMButton);

        // Create a button for saving the table and add it to the menu bar
        JButton saveButton = new JButton("Save Table");
        ActionListener save = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerTableModel.DumpPlayerFile();
            }
        };
        saveButton.addActionListener(save);
        menuBar.add(saveButton);

        JButton addButton = new JButton("Add new Player");
        ActionListener add = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPlayerDialog();
            }
        };
        addButton.addActionListener(add);
        menuBar.add(addButton);

        JButton dropButton = new JButton("Drop a Player");
        ActionListener drop = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dropPlayerDialog();
            }
        };
        dropButton.addActionListener(drop);
        menuBar.add(dropButton);

        // Add the menu bar to the frame
        content.add(menuBar, BorderLayout.NORTH);

        // Make a Table for displaying statistics on each user and the one for
        // displaying stalled threads
        table = new JTable(playerTableModel);
        table.setFillsViewportHeight(true);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(table));
        table = new JTable(stalledThreadTableModel);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table));
        content.add(panel, BorderLayout.CENTER);

        // Set the frame to be maximized and visible
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        frame.setSize(dimension);
        frame.setVisible(true);
    }

    // This prints the input text to the scroll pane at the bottom of the window
    public static void PrintLineToMainOutput(String input) {
        synchronized ((Object) outputArea) {
            outputArea.append(input + "\r\n");
        }
    }

    // Gets the parameters for adding a new player, and adds it
    public static void newPlayerDialog() {

        // Creates the dialog box to ask for the parameters
        final JDialog dialog = new JDialog();
        dialog.setTitle("Please Enter These Parameters");
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
        // Asks for the new player's main
        JPanel panel = new JPanel();
        panel.add(new JLabel("Player Main"), BorderLayout.CENTER);
        dialog.add(panel);
        panel = new JPanel();
        final JTextField main = new JTextField("", 50);
        panel.add(main, BorderLayout.CENTER);
        dialog.add(panel);
        // Asks for the new player's alt
        panel = new JPanel();
        panel.add(new JLabel("Player Alt (Leave blank for no alt.)"), BorderLayout.CENTER);
        dialog.add(panel);
        panel = new JPanel();
        final JTextField alt = new JTextField("", 50);
        panel.add(alt, BorderLayout.CENTER);
        dialog.add(panel);
        // Asks for the new player's secondary
        panel = new JPanel();
        panel.add(new JLabel("Player Secondary Alt (Leave blank for no secondary alt.)"), BorderLayout.CENTER);
        dialog.add(panel);
        panel = new JPanel();
        final JTextField sec = new JTextField("", 50);
        panel.add(sec, BorderLayout.CENTER);
        dialog.add(panel);

        // Adds OK and cancel buttons
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JButton OK = new JButton("OK");
        OK.setActionCommand("OK");
        JButton Cancel = new JButton("Cancel");
        Cancel.setActionCommand("Cancel");
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("OK")) {
                    // Parse the input and add the new player to the table and
                    // save a new table
                    if (!main.getText().trim().equals("")) {
                        String altText;
                        String secText;

                        // Parse the Alt field
                        if (alt.getText().trim().equals("")) {
                            altText = "None";
                        } else {
                            altText = alt.getText().trim();
                        }

                        // Parse the Secondary Alt field
                        if (sec.getText().trim().equals("")) {
                            secText = "None";
                        } else {
                            secText = sec.getText().trim();
                        }

                        playerTableModel.addPlayer(new Player(main.getText().trim(), altText, secText, 0, "", "", "",
                                0, 0, false, 4));
                    } // If there is some error in the input, output a message
                      // to that effect, and make no changes
                    else {
                        GUI.PrintLineToMainOutput("Error in input defining a new player. Did you spell the type right?");
                    }
                }
                // Close the dialog
                dialog.setVisible(false);
            }
        };
        OK.addActionListener(listener);
        panel.add(OK, BorderLayout.EAST);
        Cancel.addActionListener(listener);
        panel.add(Cancel, BorderLayout.EAST);
        dialog.add(panel, BorderLayout.EAST);

        // Display the dialog
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void dropPlayerDialog() {
        // Creates the dialog box to ask for the parameters
        final JDialog dialog = new JDialog();
        dialog.setTitle("Please Enter These Parameters");
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
        // Asks for the new player's name
        JPanel panel = new JPanel();
        panel.add(new JLabel("Player Main's Name"), BorderLayout.CENTER);
        dialog.add(panel);
        panel = new JPanel();
        final JTextField name = new JTextField("", 50);
        panel.add(name, BorderLayout.CENTER);
        dialog.add(panel);

        // Adds OK and cancel buttons
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JButton OK = new JButton("OK");
        OK.setActionCommand("OK");
        JButton Cancel = new JButton("Cancel");
        Cancel.setActionCommand("Cancel");
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("OK")) {
                    // Parse the input and add the new player to the table and
                    // save a new table
                    if (playerTableModel.containsPlayer(name.getText().trim())) {
                        playerTableModel.dropPlayer(playerTableModel.getPlayerByName(name.getText().trim()));
                    } else {
                        GUI.PrintLineToMainOutput("Could not drop player " + name.getText().trim()
                                + " did you spell the name right?");
                    }
                }
                // Close the dialog
                dialog.setVisible(false);
            }
        };
        OK.addActionListener(listener);
        panel.add(OK, BorderLayout.EAST);
        Cancel.addActionListener(listener);
        panel.add(Cancel, BorderLayout.EAST);
        dialog.add(panel, BorderLayout.EAST);

        // Display the dialog
        dialog.pack();
        dialog.setVisible(true);
    }

    // Asks the user if they want to save the current table, and then exits the
    // program
    public static void saveDialog() {
        int choice = JOptionPane.showConfirmDialog(null, "Save the current player table?",
                "Save the current player table?", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            playerTableModel.DumpPlayerFile();
        }
        System.exit(0);
    }

    public static void PMDialog() {
        // Check to be sure that PM's are ready to be sent
        if (PMTableModel.getRowCount() == 0) {
            GUI.PrintLineToMainOutput("The AutoPM system is not loaded with data yet, or has no PM's to send. Run a scan first.");
            return;
        }

        // Create a frame to display the AutoPM system controls
        final JFrame frame = new JFrame("AutoPM System");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container content = frame.getContentPane();

        // Make a button for sending out the PM's
        JMenuBar menuBar = new JMenuBar();
        JButton sendButton = new JButton("Send Selected PM's");
        ActionListener send = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PMTableModel.sendPM();
            }
        };
        sendButton.addActionListener(send);
        menuBar.add(sendButton);

        // Add the menu bar to the frame
        content.add(menuBar, BorderLayout.NORTH);

        // Make a Table for displaying the PM's
        final JTable table = new JTable(PMTableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        content.add(scrollPane, BorderLayout.CENTER);

        // Set the frame to be maximized and visible
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        frame.setSize(dimension);
        frame.setVisible(true);
    }

    // Gets the parameters for adding a new player, and adds it
    public static void messageEditDialog(final int row) {

        // Create a frame to display the AutoPM system controls
        final JFrame frame = new JFrame("Edit Message");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container content = frame.getContentPane();

        // Adds field to edit the message
        final JTextArea message = new JTextArea(AutoPMTableModel.PMs.get(row).getMessage());
        JScrollPane scrollPane = new JScrollPane(message);
        content.add(scrollPane, BorderLayout.CENTER);

        // Adds OK and cancel buttons
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JButton OK = new JButton("OK");
        OK.setActionCommand("OK");
        JButton Cancel = new JButton("Cancel");
        Cancel.setActionCommand("Cancel");
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("OK")) {
                    AutoPMTableModel.PMs.get(row).setMessage(message.getText());
                }
                // Close the frame
                frame.setVisible(false);
            }
        };
        OK.addActionListener(listener);
        panel.add(OK, BorderLayout.EAST);
        Cancel.addActionListener(listener);
        panel.add(Cancel, BorderLayout.EAST);
        content.add(panel, BorderLayout.NORTH);

        // Set the frame to be maximized and visible
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        frame.setSize(dimension);
        frame.setVisible(true);
    }
}
