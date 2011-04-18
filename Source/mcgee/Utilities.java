package mcgee;

import java.lang.reflect.Method;

import java.net.URL;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Utilities {

    //Sends an automated PM
    public static boolean SendAutomatedPM(AutoPM input) {
        try {
            //Create an instance of HttpClient and the default return vale
            HttpClient client = new HttpClient();
            boolean returnValue = false;

            //Make a bogus get so that we can see the session ID
            GetMethod get = new GetMethod("http://rp.mokou.org/");
            get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

            if (client.executeMethod(get) != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + get.getStatusLine());
            }
//            System.out.println(get.getResponseBodyAsString());

            //Get the session ID
            Pattern SIDPattern = Pattern.compile("sid=(\\p{Alnum}+)");
            Matcher SIDMatcher = SIDPattern.matcher(get.getResponseBodyAsString());
            SIDMatcher.find();

            //Log in
            PostMethod post = new PostMethod("http://rp.mokou.org/ucp.php?mode=login");
            post.addParameter("username", AutoPM.AutoPMUser);
            post.addParameter("password", AutoPM.AutoPMPass);
            post.addParameter("autologin", "false");
            post.addParameter("viewonline", "false");
            post.addParameter("sid", SIDMatcher.group(1));
            post.addParameter("redirect", "http://rp.mokou.org/ucp.php?i=pm&mode=compose");
            post.addParameter("login", "Login");

            if (client.executeMethod(post) != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + post.getStatusLine());
            }
//            System.out.println(post.getResponseBodyAsString());

            //Get the session ID
            SIDMatcher = SIDPattern.matcher(post.getResponseBodyAsString());
            SIDMatcher.find();

            //Now add the recipient with a second post request
            PostMethod post2 = new PostMethod("http://rp.mokou.org/ucp.php?i=pm&mode=compose&action=post&sid=" + SIDMatcher.group(1));
            post2.addParameter("username_list", input.getRecipient().getMain());
            post2.addParameter("add_to", "Add");

            if (client.executeMethod(post2) != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + post2.getStatusLine());
            }
//            System.out.println(post2.getResponseBodyAsString());

            //Find value for last click
            Pattern LastClickPattern = Pattern.compile("name=\"lastclick\" value=\"(\\p{Alnum}+)");
            Matcher LastClickMatcher = LastClickPattern.matcher(post2.getResponseBodyAsString());
            LastClickMatcher.find();

            //Find value for creation time
            Pattern CreationTimePattern = Pattern.compile("name=\"creation_time\" value=\"(\\p{Alnum}+)");
            Matcher CreationTimeMatcher = CreationTimePattern.matcher(post2.getResponseBodyAsString());
            CreationTimeMatcher.find();

            //Find value for form token
            Pattern FormTokenPattern = Pattern.compile("name=\"form_token\" value=\"(\\p{Alnum}+)");
            Matcher FormTokenMatcher = FormTokenPattern.matcher(post2.getResponseBodyAsString());
            FormTokenMatcher.find();

            //Find the address list value
            Pattern AddressPattern = Pattern.compile("name=\"(address_list[^\"]+)\"");
            Matcher AddressMatcher = AddressPattern.matcher(post2.getResponseBodyAsString());
            AddressMatcher.find();

            //Make a third post request, to actually send the PM
            PostMethod post3 = new PostMethod("http://rp.mokou.org/ucp.php?i=pm&mode=compose&action=post&sid=" + SIDMatcher.group(1));
            post3.addParameter("icon", "0");
            post3.addParameter("subject", input.getSubject());
            post3.addParameter("message", input.getMessage());
            post3.addParameter(AddressMatcher.group(1), "to");
            post3.addParameter("post", "Submit");
            post3.addParameter("disable_bbcode", "false");
            post3.addParameter("disable_magic_url", "false");
            post3.addParameter("attach_sig", "false");
            post3.addParameter("creation_time", CreationTimeMatcher.group(1));
            post3.addParameter("form_token", FormTokenMatcher.group(1));

            //Catch and handle the redirect if one occurs
            if (client.executeMethod(post3) == HttpStatus.SC_MOVED_TEMPORARILY) {
                //Handle redirect with a second get
                GetMethod get2 = new GetMethod(post3.getResponseHeader("location").getValue());
                get2.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

                if (client.executeMethod(get2) != HttpStatus.SC_OK) {
                    System.err.println("Method failed: " + get.getStatusLine());
                }
//                System.out.println(get2.getResponsebodyAsString());
            } else {
                if(post3.getResponseBodyAsString().contains("This message has been sent successfully.")){
                    returnValue = true;
                }
            }
//            System.out.println(post3.getResponsebodyAsString());

            //Logout with a third get
            GetMethod get3 = new GetMethod("http://rp.mokou.org/ucp.php?mode=logout&sid=" + SIDMatcher.group(1));
            get3.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

            if (client.executeMethod(get3) != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + get.getStatusLine());
            }
//            System.out.println(get3.getResponseBodyAsString());

            return returnValue;

        } catch (Exception e) {
            System.err.println("Problem Sending PM.");
            e.printStackTrace();
            return false;
        }
    }

    public static void OpenDefaultBrowser(String URL) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                openURL.invoke(null, new Object[]{URL});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + URL);
            } else { //assume Unix or Linux
                String[] browsers = {
                    "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, URL});
                }
            }
        } catch (Exception e) {
            System.err.println("Crash in OpenDefaultBrowser:");
            e.printStackTrace();
        }
    }

    public static long ParseDateString(String input) {
        try {
            //If the input is an empty string, return 0 minutes
            if (input == null || input.equals("")) {
                return 0;
            }

            //Turn the date into a number of minutes since Jan 1, 2009
            long Time = 0;

            //Parse Year
            int Year = Integer.parseInt(input.substring(input.indexOf(", ") + 2, input.indexOf(" ", input.indexOf(", ") + 2)).trim());

            while(Year > 2009) {
                //Add the time for Feb. 29th in leap years
                if (Year % 4 == 0) {
                    Time += 1440;
                }

                Year -= 1;
                Time += 525600;
            }

            //Parse Month
            String Month = input.substring(input.indexOf(" ") + 1, input.indexOf(" ", input.indexOf(" ") + 1)).trim();
            
            if (Month.equals("Jan")) {
            }
            else if (Month.equals("Feb")) {
                Time = Time + 44640;
            }
            else if (Month.equals("Mar")) {
                Time = Time + 40320 + 44640;
            }
            else if (Month.equals("Apr")) {
                Time = Time + 44640 + 40320 + 44640;
            }
            else if (Month.equals("May")) {
                Time = Time + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Jun")) {
                Time = Time + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Jul")) {
                Time = Time + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Aug")) {
                Time = Time + 44640 + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Sep")) {
                Time = Time + 44640 + 44640 + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Oct")) {
                Time = Time + 43200 + 44640 + 44640 + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Nov")) {
                Time = Time + 44640 + 43200 + 44640 + 44640 + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else if (Month.equals("Dec")) {
                Time = Time + 43200 + 44640 + 43200 + 44640 + 44640 + 43200 + 44640 + 43200 + 44640 + 40320 + 44640;
            }
            else {
                System.err.println("Unknown month format.");
            }

            //Parse Day
            int Day = Integer.parseInt(input.substring(input.indexOf(" ", input.indexOf(" ") + 1), input.indexOf(",")).trim());
            Time += ((Day -1) * 1440);

            //Add time for AM or PM
            if (input.contains("pm")) {
                Time += 720;
            }

            //Parse Hour
            int Hour = Integer.parseInt(input.substring(input.indexOf(" ", input.indexOf(", ") + 2),input.indexOf(":")).trim());
            Hour = Hour % 12;
            Time += (60 * Hour);

            //Parse Minute
            int Minute = Integer.parseInt(input.substring(input.indexOf(":") + 1, input.lastIndexOf(" ")).trim());
            Time += Minute;

            return Time;

        } catch (Exception e) {
            System.err.println("Crash in ParseDateString:");
            e.printStackTrace();
            return -1;
        }
    }

    //This function will perform a HTTP GET on a URL and pass the result to other functions
    public static String GetPage(URL input) {
        try {
            //Create and start a new thread to download this page
            String[] Wrapper = new String[1];
            Thread downloader = new Thread(new PageDownloader(input, Wrapper));
            downloader.start();
            //Waits for the page donwloading thread to die for at most 1 minute
            downloader.join(60000);
            //If the page has not finished by the end of 1 minute, skip it
            if (downloader.isAlive()) {
                downloader.interrupt();
            }
            return Wrapper[0];
        } catch (Exception e) {
            System.err.println("Crash in GetPage:");
            e.printStackTrace();
            return "";
        }
    }

    //Copies a file from one place to another
        public static void CopyFile(String from, String to) {
        try {
            //Create the input stream from the URL, and the output stream to the file
            FileInputStream inStream = new FileInputStream(from);
            FileOutputStream outStream = new FileOutputStream(to);
            //Do the Read/Write
            byte[] buf = new byte[1024];
            int len;
            while ((len = inStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }
            //Close the streams
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            System.err.println("Problem in CopyFile:");
            e.printStackTrace();
        }
    }
}
