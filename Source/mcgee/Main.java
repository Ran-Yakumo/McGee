package mcgee;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.net.URL;

//This class starts all of the threads doing their wonderful work.
public class Main {

    private static long LastScanTime;
    private static int MinThreadPosts;
    public static ArrayList<String> Overlimit = new ArrayList<String>();
    public static ArrayList<String> Placeholder = new ArrayList<String>();
    public static ArrayList<String> ExpiredHiatus = new ArrayList<String>();
    public static boolean scanDone = false;
    public static long CurrentTime;

    //Parses input from the config file
    public static void ReadInput() {
        try {
            //If the config file is found, read it in
            BufferedReader InFile = new BufferedReader(new FileReader("config.txt"));
            String S;
            while ((S = InFile.readLine()) != null) {
                //If this flag is seen, the next line contains the minimum number of posts that a thread can have since the last scan to be considered active
                if (S.equals("[MINTHREADPOSTS]")) {
                    S = InFile.readLine().trim();
                    MinThreadPosts = Integer.parseInt(S);
                } //If this flag is seen, the next line contains the username of the account from which automated PM's are sent
                else if (S.equals("[AUTOPMUSER]")) {
                    S = InFile.readLine().trim();
                    AutoPM.setAutoPMUser(S);
                } //If this flag is seen, the next line contains the password of the account from which automated PM's are sent
                else if (S.equals("[AUTOPMPASS]")) {
                    S = InFile.readLine().trim();
                    AutoPM.setAutoPMPass(S);
                } //If this flag is seen, the next few lines contain the default message to send in an automatic PM
                else if (S.equals("[MESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultMessage(message);
                } //If this flag is seen, the next few lines contain the default message for the first activity strike
                else if (S.equals("[ACTIVITYSTRIKEMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultActivityStrikeMessage(message);
                } //If this flag is seen, the next few lines contain the default form for a stalled thread
                else if (S.equals("[STALLEDTHREADFORM]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultStalledThreadForm(message);
                } //If this flag is seen, the next few lines contain the default message to send when a strike is removed for good activity performance
                else if (S.equals("[STRIKEREMOVEDMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultStrikeRemovedMessage(message);
                } //If this flag is seen, the next few lines contain the default message for expired hiatus warnings
                else if (S.equals("[EXPIREDHIATUSMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultExpiredHiatusMessage(message);
                } //If this flag is seen, the next few lines contian the default mssage to send when a user makes a post htat is too short
                else if (S.equals("[SHORTPOSTMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultShortPostMessage(message);
                } //If this flag is seen, the next few lines contian the default form for posts which were too short
                else if (S.equals("[SHORTPOSTFORM]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultShortPostForm(message);
                } //If this flag is seen, the next few lines contain the default message to send when a user makes a post tha tis too long
                else if (S.equals("[LONGPOSTMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultLongPostMessage(message);
                } //If this flag is seen, the next few lines contain the form for a post which was too long
                else if (S.equals("[LONGPOSTMESSAGE]")) {
                    S = InFile.readLine();
                    String message = "";
                    while (!S.equals("[ENDMESSAGE]")) {
                        message += S + "\r\n";
                        S = InFile.readLine();
                    }
                    AutoPM.setDefaultLongPostForm(message);
                }
            }
            InFile.close();

            //Now read in the player file
            InFile = new BufferedReader(new FileReader("players.txt"));

            //Read out the last scan time
            S = InFile.readLine().trim();
            LastScanTime = Long.parseLong(S);
            CurrentTime = LastScanTime;

            while ((S = InFile.readLine()) != null) {
                //Allocate resources and start by reading in the player's name
                String main;
                String alt;
                String sec;
                int threads;
                String lastPostTime;
                String lastPostThread;
                String lastPostBoard;
                int strikes;
                int goodScans;
                boolean onHiatus;
                int hiatusWeeksRemaining;

                //Read in the player's characters
                main = S.trim();
                alt = InFile.readLine().trim();
                sec = InFile.readLine().trim();

                //Read in the number of threads that the player was in at the last scan
                S = InFile.readLine().trim();
                threads = Integer.parseInt(S);

                //Read in the time of the last post in minutes since Jan 1, 2009
                lastPostTime = InFile.readLine().trim();

                //Read in the name of the last thread the character posted in
                lastPostThread = InFile.readLine().trim();

                //Read in the name of the board associated with the last thread the character posted in
                lastPostBoard = InFile.readLine().trim();

                //Read in the number of strikes the player has
                S = InFile.readLine().trim();
                strikes = Integer.parseInt(S);

                //Read in the number of consecutive good scans
                S = InFile.readLine().trim();
                goodScans = Integer.parseInt(S);

                //Read in whether or not this person is on hiatus
                S = InFile.readLine().trim();
                if (S.equals("true")) {
                    onHiatus = true;
                } else {
                    onHiatus = false;
                }

                //Read in remaining hiatus weeks
                S = InFile.readLine().trim();
                hiatusWeeksRemaining = Integer.parseInt(S);

                //Create Player object and add it to the table
                GUI.playerTableModel.addPlayer(new Player(main, alt, sec, threads, lastPostTime, lastPostThread, lastPostBoard, strikes, goodScans, onHiatus, hiatusWeeksRemaining));
            }
        } catch (Exception e) {
            System.err.println("Crash in Main's ReadInput: ");
            e.printStackTrace();
        }
    }

    static void DoScan() {
        try {
            //Welcome message
            GUI.PrintLineToMainOutput("Scanning forum with given parameters. This should take a few minutes.");

            //Find IC boards, threads, and posts
            GetForums();

            //Allocate resources
            FileWriter OutFile = new FileWriter("output.txt");

            //Write output and release resources
            OutFile.write("Stalled Threads:\r\n");
            OutFile.write(GUI.stalledThreadTableModel.toString());

            OutFile.write("\r\nOverlimit Posts:\r\n");
            for (String current : Overlimit) {
                OutFile.write(current);
            }
            OutFile.write("\r\nProbable Placeholder Posts:\r\n");
            for (String current : Placeholder) {
                OutFile.write(current);
            }
            OutFile.write("\r\nCharacters removed From Hiatus Becuase they Have No Remaining Hiatus Time:\r\n");
            for (String current : ExpiredHiatus) {
                OutFile.write(current);
            }

            GUI.playerTableModel.DumpInactivePlayers(OutFile);

            OutFile.flush();
            OutFile.close();

            //Set the flag for a scan being done to true
            scanDone = true;
        } catch (Exception e) {
            System.err.println("Crash in main: ");
            e.printStackTrace();
        }
    }

    //Finds the IC forums on the main page, and sets the current time
    static void GetForums() {
        try {
            //Get the front page of the board
            String resource = Utilities.GetPage(new URL("http://rp.mokou.org/"));

            //Find the current time, and update it
            Pattern TimePattern = Pattern.compile("It is currently([^<]*)<");
            Matcher TimeMatcher = TimePattern.matcher(resource);
            TimeMatcher.find();
            CurrentTime = Utilities.ParseDateString(TimeMatcher.group(1).trim());

            //Cut off all forums but the IC ones
            resource = resource.substring(resource.indexOf("In Character"));

            //Find the IC forums, and add them to the list
            Pattern ForumPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"forumtitle\">([^<]*)</a>");
            Matcher ForumMatcher = ForumPattern.matcher(resource);

            while (ForumMatcher.find()) {
                ProcessForum(ForumMatcher.group(2), "http://rp.mokou.org" + ForumMatcher.group(1).substring(ForumMatcher.group(1).indexOf("/"), ForumMatcher.group(1).indexOf("&amp;sid=")).replace("&amp;", "&"), true);
            }
        } catch (Exception e) {
            System.err.println("Crash in Main's GetForums: ");
            e.printStackTrace();
        }
    }

    //Parses the forums to get threads
    static void ProcessForum(String name, String URL, boolean active) {
        try {
            //Get the HTML of the page to be examined
            String resource = Utilities.GetPage(new URL(URL));

            //Get any log forums, and add them to the list of places to be crawled
            Pattern ForumPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"forumtitle\">([^<]*)</a>");
            Matcher ForumMatcher = ForumPattern.matcher(resource);

            while (ForumMatcher.find()) {
                ProcessForum(ForumMatcher.group(2), "http://rp.mokou.org" + ForumMatcher.group(1).substring(ForumMatcher.group(1).indexOf("/"), ForumMatcher.group(1).indexOf("&amp;sid=")).replace("&amp;", "&"), false);
            }

            //Cut off global announcements from the top of the forum before searching, if htere are no topics, then continue to the next forum
            if (resource.lastIndexOf("<dt>Topics</dt>") < 0) {
                return;
            }
            resource = resource.substring(resource.lastIndexOf("<dt>Topics</dt>"));

            //Find the URL's of topics
            Pattern ThreadPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"topictitle\">([^<]*)</a>");
            Matcher ThreadMatcher = ThreadPattern.matcher(resource);
            Pattern ThreadTimePattern = Pattern.compile(">([^<]*m)</span>");
            Matcher ThreadTimeMatcher = ThreadTimePattern.matcher(resource);

            while (ThreadMatcher.find() && ThreadTimeMatcher.find() && (active || Utilities.ParseDateString(ThreadTimeMatcher.group(1).trim()) > LastScanTime)) {
                ProcessThread(name, ThreadMatcher.group(2), "http://rp.mokou.org" + ThreadMatcher.group(1).substring(ThreadMatcher.group(1).indexOf("/"), ThreadMatcher.group(1).indexOf("&amp;sid=")).replace("&amp;", "&"), active);
            }

        } catch (Exception e) {
            System.err.println("Crash in Main's ParseForum: ");
            System.err.println(URL);
            e.printStackTrace();
        }
    }

    //Parses the threads to get desired data
    static void ProcessThread(String parent, String name, String URL, boolean active) {
        try {
            //Start up a set of players to have their current threads incremented
            HashSet<String> Players = new HashSet<String>();
            LinkedList<String> LastPosters = new LinkedList<String>();

            //Get the HTML of the page to be searched
            String resource = Utilities.GetPage(new URL(URL));

            //Get the number of posts in the thread
            Pattern TotalPostsPattern = Pattern.compile("(\\p{Digit}+)\\spost");
            Matcher TotalPostsMatcher = TotalPostsPattern.matcher(resource);
            TotalPostsMatcher.find();
            int totalPosts = Integer.parseInt(TotalPostsMatcher.group(1).trim());

            //Be sure to scan all the posts in the thread
            int numPosts = 0;
            for (int i = 0; 10 * i < totalPosts; i++) {

                if (i != 0) {
                    resource = Utilities.GetPage(new URL(URL + "&start=" + 10 * i));
                }

                //Find the names and times of posts
                Pattern PostPattern = Pattern.compile("<strong><a href[^>]*>([^<]*)</a>[^;]*;([^<]*)<");
                Matcher PostMatcher = PostPattern.matcher(resource);

                //Find the post content
                Pattern ContentPattern = Pattern.compile("<div class=\"content\">(.*)</div>");
                Matcher ContentMatcher = ContentPattern.matcher(resource);

                while (PostMatcher.find() && ContentMatcher.find()) {

                    //Check to be sure that this post is within the timing window
                    if (Utilities.ParseDateString(PostMatcher.group(2).trim()) > LastScanTime) {

                        //Add this post to the timing window for counting the thread active
                        numPosts += 1;

                        //Count words in the post
                        Pattern WordPattern = Pattern.compile("\\p{Alnum}+[\\s\\p{Punct}]+");
                        Matcher WordMatcher = WordPattern.matcher(ContentMatcher.group(1));
                        int wordCount = 0;
                        while (WordMatcher.find()) {
                            wordCount++;
                        }

                        //Add this post to the current count
                        if (GUI.playerTableModel.containsPlayer(PostMatcher.group(1).trim())) {
                            //If this is a placeholder post, don't count it.
                            if (wordCount < 100) {
                                if (GUI.playerTableModel.containsPlayer(PostMatcher.group(1).trim())) {
                                    GUI.playerTableModel.getPlayerByName(PostMatcher.group(1).trim()).addShortPost(new BadPost(parent, name, wordCount));
                                }
                                Placeholder.add(PostMatcher.group(1).trim() + " {" + parent + "} " + name + "\r\n");
                            } else {
                                Players.add(PostMatcher.group(1).trim());
                                GUI.playerTableModel.getPlayerByName(PostMatcher.group(1).trim()).updateLastPost(PostMatcher.group(2).trim(), name, parent);
                                //Tell the user that an overlimit post was detected
                                if (wordCount > 750) {
                                    if (GUI.playerTableModel.containsPlayer(PostMatcher.group(1).trim())) {
                                        GUI.playerTableModel.getPlayerByName(PostMatcher.group(1).trim()).addLongPost(new BadPost(parent, name, wordCount));
                                    }
                                    Overlimit.add(PostMatcher.group(1).trim() + " {" + parent + "} " + name + " \r\n");
                                }
                            }
                        } else {
                            GUI.PrintLineToMainOutput("Unrecognized Player: " + PostMatcher.group(1).trim() + ". Add this player to the config file.");
                        }
                    }

                    //Put this player into the queue of players
                    if (LastPosters.size() < 10) {
                        LastPosters.add(PostMatcher.group(1).trim());
                    } else {
                        LastPosters.poll();
                        LastPosters.add(PostMatcher.group(1).trim());
                    }
                }
            }

            //If this thread is inactive, add it to the table of stalled threads
            if (active && numPosts < MinThreadPosts) {
                GUI.stalledThreadTableModel.addStalledThread(new StalledThread(parent, name, URL, LastPosters));
            }

            //Increment thread counts for all players posting actively in this thread
            for (String current : Players) {
                GUI.playerTableModel.getPlayerByName(current).incrementThreads();
            }
        } catch (Exception e) {
            System.err.println("Crash in Main's ParseThread: " + name);
            e.printStackTrace();
        }
    }

    //Execution starts here
    public static void main(String[] args) {
        try {
            //Start the GUI
            GUI.InitializeGUI();

            //Read input from the config file
            ReadInput();
        } catch (Exception e) {
            System.err.println("Crash in main: ");
            e.printStackTrace();
        }
    }
}
