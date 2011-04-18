package mcgee;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.URL;
import java.net.Socket;

public class PageDownloader implements Runnable {

    private URL source;
    private String[] Wrapper;

    public PageDownloader(URL input, String[] inWrapper) {
        source = input;
        Wrapper = inWrapper;
    }

    public void run() {
        //Prepare to open the socket and perform GET
        Socket SKT = null;
        PrintWriter OutToSKT = null;
        BufferedReader FromSKT = null;
        String HTTPGetRequest = "GET " + source.toString() + " HTTP/1.0\r\n" + "Keep-Alive: 5\r\n";
        Wrapper[0] = "";
        char Buffer[] = new char[4096];
        int N;

        try {
            SKT = new Socket(source.getHost(), 80);
            OutToSKT = new PrintWriter(SKT.getOutputStream(), true);
            FromSKT = new BufferedReader(new InputStreamReader(SKT.getInputStream(), "UTF8"));

            //Send the GET request
            OutToSKT.println(HTTPGetRequest);

            //Read in the response
            N = FromSKT.read(Buffer, 0, 4096);
            while (N > 0) {
                String S = new String(Buffer, 0, N);
                N = FromSKT.read(Buffer, 0, 4096);
                Wrapper[0] += S;
            }

            //Close all streams, and then the socket connection
            OutToSKT.close();
            FromSKT.close();
            SKT.close();
        } catch (Exception e) {
            System.err.println("Crash in GetPage: " + e.toString() + " continuing without inspecting this page.");
            e.printStackTrace();
            Wrapper[0] = "";
        }
    }
}
