package ma.enset.chatapplication.blocking.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingTestServer extends Thread{
    private int clientCount;
    public static void main(String[] args) {
        new BlockingTestServer().start();
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(1997);
            while (true){
                Socket socket = ss.accept();
                ++clientCount;
                Conversation conversation = new Conversation(clientCount, socket);
                conversation.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class Conversation extends Thread {
        private int clientId;
        private Socket socket;

        public Conversation(int clientId, Socket socket) {
            this.clientId = clientId;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);
                String ip = socket.getRemoteSocketAddress().toString();
                System.out.println("client : " + clientId + " IpAddr : " + ip);
                pw.println("Welcome, your id is :" + clientId);
                String request;
                while ((request = br.readLine()) != null){
                    System.out.println("Request : " + request + " ; from : " + clientId + " ; Ip : " + ip);
                    String response = "Size" + request.length();
                    pw.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
