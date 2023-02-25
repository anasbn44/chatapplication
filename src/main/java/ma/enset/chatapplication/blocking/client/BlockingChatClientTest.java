package ma.enset.chatapplication.blocking.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BlockingChatClientTest {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 1997);
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = s.getOutputStream();
            PrintWriter pw = new PrintWriter(os, true);

            new Thread(()->{
                try {
                    String response;
                    while ((response = br.readLine()) != null){
                        System.out.println("response : " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while (true){
                String request = scanner.nextLine();
                pw.println(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
