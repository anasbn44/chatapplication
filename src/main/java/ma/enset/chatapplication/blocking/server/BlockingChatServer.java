package ma.enset.chatapplication.blocking.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockingChatServer extends Thread{
    private List<Conversation> conversations = new ArrayList<>();
    //List<Integer> clientIds = new ArrayList<>();
    private int clientCount;
    public static void main(String[] args) {
        new BlockingChatServer().start();
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(1997);
            while (true){
                Socket socket = ss.accept();
                ++clientCount;
                //clientIds.add(clientCount);
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String name = br.readLine();
                System.out.println(name);
                Conversation conversation = new Conversation(clientCount, socket, name);
                conversations.add(conversation);
                conversation.start();
                sendClientToAll();
                String message = conversation.clientName + " is connected";
                Broadcast(message, conversation, conversations.stream().map(c -> c.clientId).toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class Conversation extends Thread implements Serializable{
        private int clientId;
        private String clientName;
        private Socket socket;

        public Conversation(int clientId, Socket socket, String name) {
            this.clientId = clientId;
            this.socket = socket;
            this.clientName = name;
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
                    List<Integer> to = new ArrayList<>();
                    String message;
                    if(request.equals("disconnect")){
                        onDisconnect(this);
                    } else {
                        if(request.contains("=>")){
                            String[] split = request.split("=>");
                            message = split[1];
                            String ids = split[0];
                            if(ids.contains(",")){
                                String[] splitIds = ids.split(",");
                                for (String id : splitIds) {
                                    to.add(Integer.parseInt(id));
                                }
                            } else {
                                to.add(Integer.parseInt(ids));
                            }
                        } else {
                            to = conversations.stream().map(c -> c.clientId).toList();
                            message = request;
                        }
                        Broadcast(message, this, to);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "Conversation{" +
                    "clientName='" + clientName + '\'' +
                    '}';
        }
    }
    public void Broadcast (String request, Conversation from, List<Integer> to){
        try {
            for (Conversation c : conversations) {
                if(c != from && to.contains(c.clientId)){
                    Socket socket = c.socket;
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os, true);
                    pw.println(request);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClientToAll(){
        Map<Integer,String> map = conversations.stream().collect(Collectors.toMap(e -> e.clientId, e -> e.clientName));
        try {
            for (Conversation c : conversations) {
                Socket socket = c.socket;
                System.out.println(conversations);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(map);
                oos.flush();
                byte[] objectBytes = baos.toByteArray();
                String message = "clients," + Base64.getEncoder().encodeToString(objectBytes);
                pw.println(message);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onDisconnect(Conversation from) throws IOException {
        conversations.remove(from);
        from.socket.close();
        sendClientToAll();
        String message = from.clientName + " has disconnected";
        Broadcast(message, from, conversations.stream().map(c -> c.clientId).toList());
    }
}
